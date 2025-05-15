package com.IKov.TwittService.service.Impl;


import com.IKov.TwittService.entity.TwittPost;
import com.IKov.TwittService.repository.TwittRepository;
import com.IKov.TwittService.service.TwittKafkaSender;
import com.IKov.TwittService.service.TwittService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwittServiceImpl implements TwittService {

    @Value("${spring.kafka.user-topic}")
    private String userTopicName;
    @Value("${spring.kafka.index-topic}")
    private String indexTopicName;

    private final TwittRepository twittRepository;
    private final TwittKafkaSender kafkaSender;


    @Override
    public boolean postTwitt(TwittPost twittPost) {

        log.info("Posting new twitt: {}", twittPost);

        try {
            twittRepository.save(twittPost);
            log.info("Twitt saved to Cassandra: twittId={}, createdAt={}", twittPost.getTwittId(), twittPost.getCreatedAt());
        } catch (Exception e) {
            log.error("Failed to save Twitt to Cassandra", e);
            return false;
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
}
