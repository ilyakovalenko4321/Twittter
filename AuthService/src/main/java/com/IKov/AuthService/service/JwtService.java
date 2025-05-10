package com.IKov.AuthService.service;

import com.IKov.AuthService.entity.jwt.JwtTokenPair;

public interface JwtService {

    JwtTokenPair login(String tag, String password);

    JwtTokenPair renew(String accessToken, String tag);

    boolean logout(String accessToken, String refreshToken);

}
