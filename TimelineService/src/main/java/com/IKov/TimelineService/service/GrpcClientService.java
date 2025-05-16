package com.IKov.TimelineService.service;

import com.IKov.TimelineService.GetTwittsProto;
import com.IKov.TimelineService.entity.TwittPost;

import java.util.List;

public interface GrpcClientService {

    List<TwittPost> getRandomTwitts();

}
