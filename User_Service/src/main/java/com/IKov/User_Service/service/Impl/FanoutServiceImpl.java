package com.IKov.User_Service.service.Impl;

import com.IKov.User_Service.entity.subscribition.Subscribition;
import com.IKov.User_Service.entity.subscribition.SubscribitionPrimaryKey;
import com.IKov.User_Service.repository.SubscribitionRepository;
import com.IKov.User_Service.service.FanoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FanoutServiceImpl implements FanoutService {

    private final SubscribitionRepository subscribitionRepository;


    @Override
    public List<String> getSubscribersList(String userTag) {

        List<Subscribition> subscribitionList = subscribitionRepository.findAllByPersonTag(userTag);

        return subscribitionList.stream().map(
                Subscribition::getSubscribitionPrimaryKey)
                .map(SubscribitionPrimaryKey::getSubscriberTag)
                .toList();
    }
}
