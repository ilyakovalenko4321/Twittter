package com.IKov.TwittService.service.Impl.kafka;

import com.IKov.TwittService.service.TwittInteractingService;
import com.IKov.TwittService.service.TwittKafkaReceiver;
import com.IKov.TwittService.web.dto.TwittInteractingDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.kafka.receiver.KafkaReceiver;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaTwittInteractingReceiver implements TwittKafkaReceiver {

    private final KafkaReceiver<String, Object> kafkaReceiver;
    private final TwittInteractingService twittInteractingService;

    @Override
    public void receiveTwittInteraction() {

        kafkaReceiver.receive()
                .doOnNext(record -> {
                            TwittInteractingDto interactingDto = (TwittInteractingDto) record.value();
                            log.info("Receiving interaction with weight {} with twitt {}", interactingDto.getInteractionWeight(),
                                    interactingDto.getId());
                            boolean isOk =  twittInteractingService.twittInteractingHandling(interactingDto);
                            if (!isOk){
                                throw new RuntimeException("Exception while updating twitt actuality");
                            }
                        }
                )
                .doOnError(e -> log.error("Kafka error while receiving twitt interacting", e))
                .subscribe();
    }

    @PostConstruct
    void init(){
        receiveTwittInteraction();
    }
}
