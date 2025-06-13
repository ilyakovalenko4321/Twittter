package com.IKov.TimelineService.service;

import com.IKov.TimelineService.entity.twitt.TwittEntity;

import java.util.List;

public interface GrpcClientService {

    List<TwittEntity> formTimeline();

    List<TwittEntity> getRandomTwitts();

    List<TwittEntity> getTrendTwitts();

}
