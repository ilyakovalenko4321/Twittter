package com.IKov.TimelineService.service.Impl;

import com.IKov.TimelineService.GetTwittsGrpc;
import com.IKov.TimelineService.GetTwittsProto;
import com.IKov.TimelineService.entity.TwittPost;
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
    public List<TwittPost> formTimeline() {
        List<TwittPost> twittPosts = new ArrayList<>();
        twittPosts.addAll(getRandomTwitts());
        return twittPosts;
    }

    @Override
    public List<TwittPost> getRandomTwitts() {
        log.info("Запрос случайных твитов: количество = {}", randomTwittsNumber);

        GetTwittsProto.GetTwittRequest request = GetTwittsProto.GetTwittRequest.newBuilder()
                .setTwittsNumber(randomTwittsNumber)
                .build();

        log.debug("Сформирован gRPC-запрос: {}", request);

        GetTwittsProto.GetTwittReply reply;
        try {
            reply = getTwittsBlockingStub.getTwitts(request);
            log.info("Получен ответ от gRPC-сервиса: количество твитов = {}", reply.getTwitt(0).getTwittHeader());
        } catch (Exception e) {
            log.error("Ошибка при вызове gRPC-сервиса: {}", e.getMessage(), e);
            throw e;
        }

        List<TwittPost> twittPostList = TwittPostMapper.toDomainList(reply);
        log.debug("Преобразование в доменные объекты завершено: {}", twittPostList);

        return twittPostList;
    }

}
