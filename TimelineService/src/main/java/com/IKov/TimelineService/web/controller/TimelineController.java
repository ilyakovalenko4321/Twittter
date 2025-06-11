package com.IKov.TimelineService.web.controller;

import com.IKov.TimelineService.entity.twitt.TwittEntity;
import com.IKov.TimelineService.service.GrpcClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/timeline")
public class TimelineController {

    private final GrpcClientService grpcClientService;

    @GetMapping("form")
    public List<TwittEntity> formTimeline(){
        return grpcClientService.formTimeline();
    }

}
