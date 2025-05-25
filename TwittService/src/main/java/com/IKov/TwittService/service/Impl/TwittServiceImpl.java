package com.IKov.TwittService.service.Impl;


import com.IKov.TwittService.entity.TwittPost;
import com.IKov.TwittService.entity.exceptions.CassandraException;
import com.IKov.TwittService.entity.exceptions.RedisException;
import com.IKov.TwittService.repository.TwittRepository;
import com.IKov.TwittService.service.TwittKafkaSender;
import com.IKov.TwittService.service.TwittService;
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
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwittServiceImpl implements TwittService {

    @Value("${spring.kafka.user-topic}")
    private String userTopicName;
    @Value("${spring.kafka.index-topic}")
    private String indexTopicName;
    @Value("${configs.redis.randomly-recommended-days}")
    private Integer randomlyRecommendedDays;
    @Value("${configs.redis.random-twitt-prefix}")
    private String randomTwittPrefix;

    private final TwittRepository twittRepository;
    private final TwittKafkaSender kafkaSender;
    private final RedisTemplate<String, Object> redisTemplate;


    @Override
    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public boolean postTwitt(TwittPost twittPost) {

        log.info("Posting new twitt: {}", twittPost);
        try {
            twittRepository.save(twittPost);
            log.info("Twitt saved to Cassandra: twittId={}, createdAt={}", twittPost.getTwittId(), twittPost.getCreatedAt());
        } catch (Exception e) {
            log.error("Failed to save Twitt to Cassandra", e);
            throw new CassandraException("Error while saving in Cassandra");
        }

        try{
            redisTemplate.opsForValue().set(String.valueOf(randomTwittPrefix + ":" +twittPost.getTwittId()), "1", Duration.ofDays(randomlyRecommendedDays));
        }catch (Exception e){
            log.error("Exception while saving post with UUID={} to Redis. It will be unable to select it randomly", twittPost.getTwittId());
            throw new RedisException("Error while saving in Redis");
        }

        kafkaSender.send(userTopicName, Map.of(twittPost.getUserTag(), twittPost.getTwittId()))
                .doOnSubscribe(s -> log.info("Sending to Kafka user topic: {} -> {}", twittPost.getUserTag(), twittPost.getTwittId()))
                .doOnSuccess(v -> log.info("Successfully sent to Kafka user topic"))
                .doOnError(e -> log.error("Failed to send to Kafka user topic", e))
                .subscribe();

        Map<String, Object> indexSendMap = new HashMap<>();
        indexSendMap.put("twittId", twittPost.getTwittId());
        indexSendMap.put("twittText", twittPost.getTwittText());
        indexSendMap.put("twittTags", twittPost.getTwittTags());

        kafkaSender.send(indexTopicName, indexSendMap)
                .doOnSubscribe(s -> log.info("Sending to Kafka index topic: {}", indexSendMap))
                .doOnSuccess(v -> log.info("Successfully sent to Kafka index topic"))
                .doOnError(e -> log.error("Failed to send to Kafka index topic", e))
                .subscribe();

        return true;
    }

    @Recover
    public boolean recover(Exception e, TwittPost twittPost){
        log.error("Attempts to post twitt with tag={} are over", twittPost.getTwittHeader());
        return false;
    }

    @Override
    public List<TwittPost> formRandomTwittStack(Integer n) {
        log.info("Начало формирования случайного стека твитов. Требуется: {}", n);

        List<UUID> candidateKeys = new ArrayList<>();
        ScanOptions scanOptions = ScanOptions.scanOptions()
                .match(randomTwittPrefix + "*")
                .count(1000)
                .build();

        try (Cursor<byte[]> cursor = redisTemplate.getConnectionFactory().getConnection().scan(scanOptions)) {
            while (cursor.hasNext()) {
                String keyExtended = new String(cursor.next());
                String key = keyExtended.substring(keyExtended.indexOf(":") + 1);

                try {
                    UUID uuid = UUID.fromString(key);
                    candidateKeys.add(uuid);
                } catch (IllegalArgumentException ex) {
                    log.warn("Не удалось преобразовать ключ '{}' в UUID", key);
                    continue;
                }

                if (candidateKeys.size() >= n) break;
            }

            log.info("Найдено {} подходящих ключей для твитов", candidateKeys.size());
            if (candidateKeys.isEmpty()) {
                log.warn("Список ключей для выборки твитов пуст. Вероятно, Redis не содержит подходящих записей.");
            } else {
                log.debug("Первые ключи для выборки: {}", candidateKeys.stream().limit(5).toList());
            }

        } catch (Exception e) {
            log.error("Ошибка при сканировании Redis для выбора твитов: {}", e.getMessage(), e);
            throw new RedisException("Ошибка при сканировании Redis");
        }

        List<TwittPost> result = twittRepository.findAllByTwittIdIn(candidateKeys);
        log.info("Извлечено {} твитов из twittRepository", result.size());

        return result;
    }

}
