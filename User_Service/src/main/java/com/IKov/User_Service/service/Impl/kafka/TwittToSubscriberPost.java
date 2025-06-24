package com.IKov.User_Service.service.Impl.kafka;

import com.IKov.User_Service.web.dto.TwittToSubscribersDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwittToSubscriberPost {

    @Value("${spring.kafka.indexes.twitt-to-users}")
    public String twittToUsers;

    private final KafkaSender<String, Object> twittToUsersSender;

    public void handlePostingTwittToSubscribers(UUID twittUUID, List<String> subscribersList){

        TwittToSubscribersDto twittToSubscribersDto = TwittToSubscribersDto.builder()
                .twittUUID(twittUUID)
                .subscribersList(subscribersList)
                .build();

        ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(twittToUsers, null, twittToUsers);
        SenderRecord<String, Object, Void> senderRecord = SenderRecord.create(producerRecord, null);

        twittToUsersSender.send(Mono.just(senderRecord))
                .then()
                .subscribe();
    }

}
