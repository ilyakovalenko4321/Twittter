package com.IKov.User_Service.service.Impl;


import com.IKov.User_Service.entity.Profile.Profile;
import com.IKov.User_Service.entity.Profile.Status;
import com.IKov.User_Service.repository.ProfileRepository;
import com.IKov.User_Service.service.MailSender;
import com.IKov.User_Service.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final MailSender mailSender;
    private final ProfileRepository profileRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean saveProfile(Profile profile) {

        Random random = new Random();
        Integer confirmationCode = random.nextInt(100000, 999999);

        profile.setPassword(passwordEncoder.encode(profile.getPassword()));

        profile.setStatus(Status.UNCONFIRMED);
        Profile newProfile = profileRepository.save(profile);
        redisTemplate.opsForValue().set(profile.getEmail(), confirmationCode);
        redisTemplate.expire(profile.getEmail(), 30, TimeUnit.MINUTES);

        mailSender.sendAuthConfirmation(profile.getEmail(), confirmationCode);

        return newProfile.getId() != null;
    }

    @Override
    public boolean confirmProfile(String email, Integer confirmationCode) {
        Integer realConfirmationCode = (Integer) redisTemplate.opsForValue().get(email);
        if (Objects.equals(confirmationCode, realConfirmationCode)) {
            profileRepository.updateProfileStatus(email, Status.CONFIRMED);
            return true;
        }
        return false;
    }

}
