package com.IKov.TwittService.service;

import com.IKov.TwittService.entity.twitt.TwittEntity;

import java.util.List;

public interface TwittService {

    boolean postTwitt(TwittEntity twittEntity);

    List<TwittEntity> formRandomTwittStack(Integer n);

    List<TwittEntity> formTrendTwittStack(Integer n);

}
