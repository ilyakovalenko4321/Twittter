package com.IKov.TwittService.service;

import com.IKov.TwittService.GetTwittsProto;

public interface RandomTwittService {

    GetTwittsProto.GetTwittRandomReply getRandomTwitts(Integer number);

    GetTwittsProto.GetTwittTrendReply getTrendTwitts(Integer number);

}
