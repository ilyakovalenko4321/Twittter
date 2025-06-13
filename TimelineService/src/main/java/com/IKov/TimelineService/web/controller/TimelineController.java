package com.IKov.TimelineService.web.controller;

import com.IKov.TimelineService.entity.twitt.TwittEntity;
import com.IKov.TimelineService.entity.user.UserTag;
import com.IKov.TimelineService.service.GrpcClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/timeline")
public class TimelineController {

    private final GrpcClientService grpcClientService;

    @PostMapping("form")
    public List<TwittEntity> formTimeline(@RequestBody UserTag userTag){
        return grpcClientService.formTimeline(userTag.getUserTag());
    }

}
