package com.IKov.TwittService.service.Impl;


import com.IKov.TwittService.service.TwittKafkaSender;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderRecord;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TwittKafkaSenderImpl implements TwittKafkaSender {

    private final reactor.kafka.sender.KafkaSender<String, Object> kafkaSender;

    @Override
    public Mono<Void> send(String sendTo, Map<String, Object> value) {
        ProducerRecord<String, Object> producerRecord =
                new ProducerRecord<>(sendTo, null, value); // key = null, value = Map

        SenderRecord<String, Object, Void> senderRecord =
                SenderRecord.create(producerRecord, null); // correlation metadata = null

        return kafkaSender
                .send(Mono.just(senderRecord))
                .then();
    }


}
