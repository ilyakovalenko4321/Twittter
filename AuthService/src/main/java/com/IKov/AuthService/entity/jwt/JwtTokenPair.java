package com.IKov.AuthService.entity.jwt;

import lombok.Data;

@Data
public class JwtTokenPair {

    private String accessToken;
    private String refreshToken;

}
