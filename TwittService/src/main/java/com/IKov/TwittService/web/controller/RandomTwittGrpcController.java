package com.IKov.TwittService.web.controller;


import com.IKov.TwittService.GetTwittsGrpc.GetTwittsImplBase;
import com.IKov.TwittService.GetTwittsProto;
import com.IKov.TwittService.service.RandomTwittService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class RandomTwittGrpcController extends GetTwittsImplBase {

    private final RandomTwittService randomTwittService;


    @Override
    public void getTwitts(GetTwittsProto.GetTwittRandomRequest request,
                          StreamObserver<GetTwittsProto.GetTwittRandomReply> responseObserver) {
        log.info("gRPC request: get {} twitts", request.getTwittsNumber());

        GetTwittsProto.GetTwittRandomReply reply;
        try {
            reply = randomTwittService.getRandomTwitts(request.getTwittsNumber());
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
            log.info("gRPC response sent ({} twitts)", reply.getTwittCount());
        } catch (Exception ex) {
            log.error("Error in getTwitts", ex);
            responseObserver.onError(
                    io.grpc.Status.INTERNAL
                            .withDescription("Internal error: " + ex.getMessage())
                            .asRuntimeException());
        }
    }
}
