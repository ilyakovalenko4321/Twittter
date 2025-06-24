package com.IKov.User_Service.web.controller.kafka;

import com.IKov.User_Service.entity.subscribition.Subscribition;
import com.IKov.User_Service.service.SubscribitionService;
import com.IKov.User_Service.web.dto.SubscribitionDto;
import com.IKov.User_Service.web.mapping.SubscribitionMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.kafka.receiver.KafkaReceiver;

@Component
@RequiredArgsConstructor
public class SubscribitionController {

    private final KafkaReceiver<String, Object> subscribitionReceiver;
    private final SubscribitionService subscribitionService;

    @PostConstruct
    public void handleSubscribition(){
        subscribitionReceiver.receive()
                .doOnNext(record -> {
                    SubscribitionDto subscribitionDto = (SubscribitionDto) record.value();
                    Subscribition subscribition = SubscribitionMapper.toEntity(subscribitionDto);
                    if(subscribitionDto.isSubscribe()){
                        subscribitionService.handleSubscribition(subscribition);
                    } else {
                        subscribitionService.handleUnsubscribe(subscribition);
                    }
                })
                .subscribe();
    }

}
