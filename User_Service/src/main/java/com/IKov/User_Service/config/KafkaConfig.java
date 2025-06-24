package com.IKov.User_Service.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.Collections;
import java.util.HashMap;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    @Value("${spring.kafka.indexes.subscribition}")
    public String subscribitionIndex;
    @Value("${spring.kafka.indexes.user-topic}")
    public String indexTopic;
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${spring.kafka.indexes.twitt-to-users}")
    public String twittToUsers;

    @Bean
    public NewTopic userTopicName(){
        return TopicBuilder
                .name(twittToUsers)
                .partitions(5)
                .replicas(1)
                .build();
    }

    @Bean
    public ReceiverOptions<String, Object> receiverOptions(){
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        hashMap.put(ConsumerConfig.GROUP_ID_CONFIG, "user_microservice");
        hashMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        hashMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        hashMap.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return ReceiverOptions.create(hashMap);
    }

    @Bean
    public KafkaReceiver<String, Object> subscribitionReceiver(){
        return KafkaReceiver.create(receiverOptions().subscription(Collections.singleton(subscribitionIndex)));
    }

    @Bean
    public KafkaReceiver<String, Object> twittPostReceiver(){
        return KafkaReceiver.create(receiverOptions().subscription(Collections.singleton(indexTopic)));
    }

    @Bean
    public SenderOptions<String, Object> senderOptions(){
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        hashMap.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        hashMap.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return SenderOptions.create(hashMap);
    }

    @Bean
    public KafkaSender<String, Object> twittToUsersSender(){
        return KafkaSender.create(senderOptions());
    }

}
