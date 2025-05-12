package com.IKov.TwittService.service.Impl;


import com.IKov.TwittService.entity.TwittPost;
import com.IKov.TwittService.repository.TwittRepository;
import com.IKov.TwittService.service.TwittKafkaSender;
import com.IKov.TwittService.service.TwittService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TwittServiceImpl implements TwittService {

    @Value("${spring.kafka.user-topic}")
    private String userTopicName;
    @Value("${spring.kafka.index-topic}")
    private String indexTopicName;

    private final TwittRepository twittRepository;
    private final TwittKafkaSender kafkaSender;


    @Override
    public boolean postTwitt(TwittPost twittPost) {

        twittRepository.save(twittPost);
        kafkaSender.send(userTopicName, Map.of(twittPost.getUserTag(), twittPost.getTwittId())).subscribe();

        Map<String, Object> indexSendMap = new HashMap<>();
        indexSendMap.put("twittId", twittPost.getTwittId());
        indexSendMap.put("twittText", twittPost.getTwittText());
        indexSendMap.put("twittTags", twittPost.getTwittTags());

        kafkaSender.send(indexTopicName, indexSendMap).subscribe();

        return false;
    }
}
