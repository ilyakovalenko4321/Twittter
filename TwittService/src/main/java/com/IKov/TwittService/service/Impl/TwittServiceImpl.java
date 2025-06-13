package com.IKov.TwittService.service.Impl;


import com.IKov.TwittService.entity.twitt.TwittEntity;
import com.IKov.TwittService.entity.exceptions.CassandraException;
import com.IKov.TwittService.entity.exceptions.RedisException;
import com.IKov.TwittService.repository.CassandraTwittRepository;
import com.IKov.TwittService.service.TwittKafkaSender;
import com.IKov.TwittService.service.TwittService;
import jnr.ffi.annotations.In;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwittServiceImpl implements TwittService {

    @Value("${spring.kafka.user-topic}")
    private String userTopicName;
    @Value("${spring.kafka.index-topic}")
    private String indexTopicName;
    @Value("${configs.redis.trend-recommended-days}")
    private Integer trendRecommendedDays;
    @Value("${configs.redis.trend-twitt-prefix}")
    private String trendTwittPrefix;
    @Value("${configs.redis.random-key-set-prefix}")
    private String randomKeySetPrefix;
    @Value("${configs.redis.trend-key-set-prefix}")
    private String trendKeySetPrefix;

    private final CassandraTwittRepository cassandraTwittRepository;
    private final TwittKafkaSender kafkaSender;
    private final RedisTemplate<String, Object> redisTemplate;


    @Override
    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public boolean postTwitt(TwittEntity twittEntity) {

        log.info("Posting new twitt: {}", twittEntity);
        try {
            cassandraTwittRepository.save(twittEntity);
            log.info("Twitt saved to Cassandra: twittId={}, createdAt={}", twittEntity.getTwittId(), twittEntity.getCreatedAt());
        } catch (Exception e) {
            log.error("Failed to save Twitt to Cassandra", e);
            throw new CassandraException("Error while saving in Cassandra");
        }

        try{
            redisTemplate.opsForSet().add(String.valueOf(randomKeySetPrefix), String.valueOf(twittEntity.getTwittId()));
        } catch (Exception ex){
            log.error("Exception while saving post uuid into randomKeyPrefix set. It will be impossible to promote it by chance");
        }

        try{
            String key = trendTwittPrefix + twittEntity.getTwittId();

            Instant now = Instant.now();
            String timestamp = now.getEpochSecond() + "." + now.getNano();
            double initialScore = 0.0;
            redisTemplate.opsForList().rightPushAll(key, timestamp, initialScore);
            redisTemplate.expire(key, Duration.ofDays(trendRecommendedDays));
        }catch (Exception e){
            log.error("Exception while saving post with UUID={} to Redis. It will be unable to select it in trends", twittEntity.getTwittId());
            throw new RedisException("Error while saving in Redis");
        }

        kafkaSender.send(userTopicName, Map.of(twittEntity.getUserTag(), twittEntity.getTwittId()))
                .doOnSubscribe(s -> log.info("Sending to Kafka user topic: {} -> {}", twittEntity.getUserTag(), twittEntity.getTwittId()))
                .doOnSuccess(v -> log.info("Successfully sent to Kafka user topic"))
                .doOnError(e -> log.error("Failed to send to Kafka user topic", e))
                .subscribe();

        Map<String, Object> indexSendMap = new HashMap<>();
        indexSendMap.put("twittId", twittEntity.getTwittId());
        indexSendMap.put("twittText", twittEntity.getTwittText());
        indexSendMap.put("twittTags", twittEntity.getTwittTags());

        kafkaSender.send(indexTopicName, indexSendMap)
                .doOnSubscribe(s -> log.info("Sending to Kafka index topic: {}", indexSendMap))
                .doOnSuccess(v -> log.info("Successfully sent to Kafka index topic"))
                .doOnError(e -> log.error("Failed to send to Kafka index topic", e))
                .subscribe();

        return true;
    }

    @Recover
    public boolean recover(Exception ignoredE, TwittEntity twittEntity){
        log.error("Attempts to post twitt with tag={} are over", twittEntity.getTwittHeader());
        return false;
    }

    @Override
    public List<TwittEntity> formRandomTwittStack(Integer n) {
        log.info("Начало формирования случайного стека твитов. Требуется: {}", n);

        List<UUID> candidateKeys;

        candidateKeys = Objects.requireNonNull(redisTemplate.opsForSet().distinctRandomMembers(randomKeySetPrefix, n))
                .stream()
                .map(obj -> UUID.fromString((String) obj))
                .toList();

        List<TwittEntity> result = cassandraTwittRepository.findAllByTwittIdIn(candidateKeys);
        log.info("Извлечено {} рандомных твитов из twittRepository", result.size());

        return result;
    }

    @Override
    public List<TwittEntity> formTrendTwittStack(Integer n) {
        log.info("Начало формирования стека трендовых твитов. Требуется: {}", n);

        List<UUID> candidateKeys = Objects.requireNonNull(redisTemplate.opsForSet().distinctRandomMembers(trendKeySetPrefix, n))
                .stream()
                .map(obj -> UUID.fromString((String) obj))
                .toList();

        List<TwittEntity> resultList = cassandraTwittRepository.findAllByTwittIdIn(candidateKeys);
        log.info("Извлечено {} трендовых твитов из twittRepository", resultList.size());

        return resultList;
    }

}
