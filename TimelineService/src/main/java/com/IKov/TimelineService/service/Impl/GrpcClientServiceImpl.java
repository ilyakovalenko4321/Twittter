package com.IKov.TimelineService.service.Impl;

import com.IKov.TimelineService.GetTwittsGrpc;
import com.IKov.TimelineService.GetTwittsProto;
import com.IKov.TimelineService.entity.twitt.TwittEntity;
import com.IKov.TimelineService.service.GrpcClientService;
import com.IKov.TimelineService.web.mapper.TwittPostMapper;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class GrpcClientServiceImpl implements GrpcClientService {

    @GrpcClient("myGrpcService")
    private GetTwittsGrpc.GetTwittsBlockingStub getTwittsBlockingStub;

    @Value("${config.grpc.random-twitts-number}")
    private Integer randomTwittsNumber;

    @Override
    public List<TwittEntity> formTimeline() {
        List<TwittEntity> twittEntities = new ArrayList<>();
        twittEntities.addAll(getRandomTwitts());
        return twittEntities;
    }

    @Override
    public List<TwittEntity> getRandomTwitts() {
        log.info("Запрос случайных твитов: количество = {}", randomTwittsNumber);

        GetTwittsProto.GetTwittRandomRequest request = GetTwittsProto.GetTwittRandomRequest.newBuilder()
                .setTwittsNumber(randomTwittsNumber)
                .build();

        log.debug("Сформирован gRPC-запрос: {}", request);

        GetTwittsProto.GetTwittRandomReply reply;
        try {
            reply = getTwittsBlockingStub.getRandomTwitts(request);
            log.info("Получен ответ от gRPC-сервиса: количество твитов = {}", reply.getTwitt(0).getTwittHeader());
        } catch (Exception e) {
            log.error("Ошибка при вызове gRPC-сервиса: {}", e.getMessage(), e);
            throw e;
        }

        List<TwittEntity> twittEntityList = TwittPostMapper.toDomainList(reply);
        log.debug("Преобразование в доменные объекты завершено: {}", twittEntityList);

        return twittEntityList;
    }

}
