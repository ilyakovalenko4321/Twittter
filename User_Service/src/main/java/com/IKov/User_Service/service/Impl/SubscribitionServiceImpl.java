package com.IKov.User_Service.service.Impl;

import com.IKov.User_Service.entity.subscribition.Subscribition;
import com.IKov.User_Service.repository.SubscribitionNumberRepository;
import com.IKov.User_Service.repository.SubscribitionRepository;
import com.IKov.User_Service.service.SubscribitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscribitionServiceImpl implements SubscribitionService {

    private final SubscribitionRepository subscribitionRepository;
    private final SubscribitionNumberRepository subscribitionNumberRepository;

    @Override
    public void handleSubscribition(Subscribition subscribition) {
        subscribitionRepository.save(subscribition);
        subscribitionNumberRepository.increment(subscribition.getSubscribitionPrimaryKey().getPersonTag());
    }

    @Override
    public void handleUnsubscribe(Subscribition subscribition) {
        subscribitionRepository.delete(subscribition);
        subscribitionNumberRepository.decrement(subscribition.getSubscribitionPrimaryKey().getPersonTag());
    }
}
