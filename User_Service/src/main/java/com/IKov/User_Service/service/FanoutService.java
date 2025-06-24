package com.IKov.User_Service.service;

import java.util.List;

public interface FanoutService {

    List<String> getSubscribersList(String userTag);

}
