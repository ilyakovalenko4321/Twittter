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
    @Value("${config.grpc.trend-twitts-number}")
    private Integer trendTwittsNumber;

    @Override
    public List<TwittEntity> formTimeline() {
        List<TwittEntity> twittEntities = new ArrayList<>();
        twittEntities.addAll(getRandomTwitts());
        twittEntities.addAll(getTrendTwitts());
        return twittEntities;
    }

    @Override
    public List<TwittEntity> getRandomTwitts() {
        log.info("Запрос случайных твитов: количество = {}", randomTwittsNumber);

        GetTwittsProto.GetTwittRandomRequest request = GetTwittsProto.GetTwittRandomRequest.newBuilder()
                .setTwittsNumber(randomTwittsNumber)
                .build();

        log.debug("Сформирован gRPC-запрос на получение рандомных твитов: {}", request);

        GetTwittsProto.GetTwittRandomReply reply;
        try {
            reply = getTwittsBlockingStub.getRandomTwitts(request);
            log.info("Получен ответ от gRPC-сервиса: количество рандомных твитов = {}", reply.getTwitt(0).getTwittHeader());
        } catch (Exception e) {
            log.error("Ошибка при вызове gRPC-сервиса: {}", e.getMessage(), e);
            throw e;
        }

        List<TwittEntity> twittEntityList = TwittPostMapper.toDomainListRandom(reply);
        log.debug("Преобразование в доменные объекты завершено: {}", twittEntityList);

        return twittEntityList;
    }

    @Override
    public List<TwittEntity> getTrendTwitts() {
        log.info("Запрос трендовых твитов: количество = {}", trendTwittsNumber);

        GetTwittsProto.GetTwitTrendRequest request = GetTwittsProto.GetTwitTrendRequest.newBuilder()
                .setTwittNumber(trendTwittsNumber)
                .build();

        log.debug("Сформирован gRPC-запрос на получение трендовых твитов: {}", request);

        GetTwittsProto.GetTwittTrendReply reply;
        try{
            reply = getTwittsBlockingStub.getTrendTwitts(request);
            log.info("Получен ответ от gRPC-сервиса: количество трендовых твитов = {}", reply.getTwitt(0).getTwittHeader());
        } catch (Exception e){
            log.error("Ошибка при вызове gRPC-сервиса: {}", e.getMessage(), e);
            throw e;
        }

        List<TwittEntity> twittEntityList = TwittPostMapper.toDomainListTrend(reply);
        log.debug("Преобразование в доменные объекты завершено: {}", twittEntityList);

        return twittEntityList;
    }

}
