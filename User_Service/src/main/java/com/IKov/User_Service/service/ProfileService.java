package com.IKov.User_Service.service;

import com.IKov.User_Service.entity.Profile.Profile;

public interface ProfileService {

    boolean saveProfile(Profile profile);

    boolean confirmProfile(String email, Integer confirmationCode);

}
