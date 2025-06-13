package com.IKov.TwittService.service.Impl;

import com.IKov.TwittService.GetTwittsProto;
import com.IKov.TwittService.entity.twitt.TwittEntity;
import com.IKov.TwittService.service.RandomTwittService;
import com.IKov.TwittService.service.TwittService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RandomTwittServiceImpl implements RandomTwittService {

    private final TwittService twittService;

    @Override
    public GetTwittsProto.GetTwittRandomReply getRandomTwitts(Integer number) {
        List<TwittEntity> twittEntityList = twittService.formRandomTwittStack(number);

        //Приведение к типу reply
        GetTwittsProto.GetTwittRandomReply.Builder replyBuilder = GetTwittsProto.GetTwittRandomReply.newBuilder();
        for(TwittEntity twittEntity: twittEntityList){
            replyBuilder.addTwitt(mapToProto(twittEntity));
        }

        return replyBuilder.build();
    }

    @Override
    public GetTwittsProto.GetTwittTrendReply getTrendTwitts(Integer number) {
        List<TwittEntity> twittEntityList = twittService.formTrendTwittStack(number);

        GetTwittsProto.GetTwittTrendReply.Builder reply = GetTwittsProto.GetTwittTrendReply.newBuilder();
        for (TwittEntity twittEntity : twittEntityList){
            reply.addTwitt(mapToProto(twittEntity));
        }

        return reply.build();
    }

    private GetTwittsProto.Twitt mapToProto(TwittEntity e) {
        return GetTwittsProto.Twitt.newBuilder()
                .setUserTag(e.getUserTag())
                .setTwittText(e.getTwittText())
                .setTwittHeader(e.getTwittHeader())
                .addAllTwittTags(e.getTwittTags() == null ? List.of() : e.getTwittTags())
                .setCreatedAt(com.google.protobuf.Timestamp.newBuilder()
                        .setSeconds(e.getCreatedAt().toEpochSecond(ZoneOffset.UTC))
                        .setNanos(e.getCreatedAt().getNano())
                        .build())
                .build();
    }
}
