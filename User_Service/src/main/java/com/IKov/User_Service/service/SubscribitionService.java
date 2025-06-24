package com.IKov.User_Service.service;

import com.IKov.User_Service.entity.subscribition.Subscribition;

public interface SubscribitionService {

    void handleSubscribition(Subscribition subscribition);

    void handleUnsubscribe(Subscribition subscribition);

}
