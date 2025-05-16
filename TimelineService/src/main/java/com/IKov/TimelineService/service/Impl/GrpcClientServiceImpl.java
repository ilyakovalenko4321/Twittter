package com.IKov.TimelineService.service.Impl;

import com.IKov.TimelineService.GetTwittsGrpc;
import com.IKov.TimelineService.GetTwittsProto;
import com.IKov.TimelineService.entity.TwittPost;
import com.IKov.TimelineService.service.GrpcClientService;
import com.IKov.TimelineService.web.mapper.TwittPostMapper;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GrpcClientServiceImpl implements GrpcClientService {

    @GrpcClient("myGrpcService")
    private GetTwittsGrpc.GetTwittsBlockingStub getTwittsBlockingStub;

    @Value("${config.grpc.random-twitts-number}")
    private Integer randomTwittsNumber;

    @Override
    public List<TwittPost> getRandomTwitts() {

        GetTwittsProto.GetTwittRequest request = GetTwittsProto.GetTwittRequest.newBuilder()
                .setTwittsNumber(randomTwittsNumber)
                .build();
        GetTwittsProto.GetTwittReply reply = getTwittsBlockingStub.getTwitts(request);
        List<TwittPost> twittPostList = TwittPostMapper.toDomainList(reply);
        return twittPostList;
    }



}
