package com.IKov.TwittService.service.Impl;


import com.IKov.TimelineService.GetTwittsGrpc.GetTwittsImplBase;
import com.IKov.TimelineService.GetTwittsProto;
import com.IKov.TwittService.entity.TwittPost;
import com.IKov.TwittService.service.TwittService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.ZoneOffset;
import java.util.List;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class FormRandomTwittStack extends GetTwittsImplBase{

    private final TwittService twittService;

    @Override
    public void getTwitts(GetTwittsProto.GetTwittRequest request, StreamObserver<GetTwittsProto.GetTwittReply> replyStreamObserver){
        List<TwittPost> twittPostList = twittService.formRandomTwittStack(request.getTwittsNumber());

        GetTwittsProto.GetTwittReply.Builder getTwittReplyBuilder =  GetTwittsProto.GetTwittReply.newBuilder();
        for(TwittPost twittPost: twittPostList){
            GetTwittsProto.Twitt twitt = GetTwittsProto.Twitt.newBuilder()
                    .setUserTag("TEST_TAG")
                    .setTwittText(twittPost.getTwittText())
                    .setTwittHeader(twittPost.getTwittHeader())
                    .addAllTwittTags(twittPost.getTwittTags())
                    .setCreatedAt(
                            com.google.protobuf.Timestamp.newBuilder()
                                    .setSeconds(twittPost.getCreatedAt().toEpochSecond(ZoneOffset.UTC))
                                    .setNanos(twittPost.getCreatedAt().getNano())
                                    .build()
                    )
                    .build();

            getTwittReplyBuilder.addTwitt(twitt);
        }

        replyStreamObserver.onNext(getTwittReplyBuilder.build());
        log.info("Sent {} twitts in response to request {}", twittPostList.size(), request);


        replyStreamObserver.onCompleted();
        log.info("StreamObserver.onCompleted() called for request {}", request);
    }

}
