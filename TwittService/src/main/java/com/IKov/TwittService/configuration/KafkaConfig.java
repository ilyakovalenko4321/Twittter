package com.IKov.TwittService.configuration;

import com.fasterxml.jackson.databind.JsonSerializer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;


@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    @Value("${spring.kafka.user-topic}")
    private String userTopicName;
    @Value("${spring.kafka.index-topic}")
    private String indexTopicName;
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public NewTopic userTopicName(){
        return TopicBuilder
                .name(userTopicName)
                .partitions(5)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic indexTopicName(){
        return TopicBuilder
                .name(indexTopicName)
                .partitions(5)
                .replicas(1)
                .build();
    }

    @Bean
    public SenderOptions<String, Object> senderOptions(){
        Map<String, Object> senderOptions = new HashMap<>();

        senderOptions.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        senderOptions.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        senderOptions.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return SenderOptions.create(senderOptions);
    }

    @Bean
    public KafkaSender<String, Object> sender(){
        return KafkaSender.create(senderOptions());
    }
}
