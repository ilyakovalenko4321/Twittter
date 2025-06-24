package com.IKov.User_Service.web.controller.kafka;

import com.IKov.User_Service.service.FanoutService;
import com.IKov.User_Service.service.Impl.kafka.TwittToSubscriberPost;
import com.IKov.User_Service.web.dto.TwittPostDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.kafka.receiver.KafkaReceiver;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FanoutController {

    private final KafkaReceiver<String, Object> twittPostReceiver;
    private final FanoutService fanoutService;
    private final TwittToSubscriberPost twittToSubscriberPost;

    @PostConstruct
    public void handleTwittPublishing(){
        twittPostReceiver.receive()
                .doOnNext(record -> {
                    Map<String, Object> map = (Map<String, Object>) record.value();

                    Map.Entry<String, Object> entry = map.entrySet().iterator().next();
                    String userTag = entry.getKey();
                    Object twittId = entry.getValue();
                    TwittPostDto twittPostDto = new TwittPostDto();
                    twittPostDto.setTwittId(UUID.fromString((String) twittId));
                    twittPostDto.setUserTag(userTag);
                    List<String> subscribersList = fanoutService.getSubscribersList(twittPostDto.getUserTag());
                    twittToSubscriberPost.handlePostingTwittToSubscribers(twittPostDto.getTwittId(), subscribersList);
                    log.info("Start subscribers collection for user with id : {}", twittPostDto.getUserTag());
                })
                .subscribe();
    }

}
