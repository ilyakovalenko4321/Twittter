package com.IKov.AuthService.entity.jwt;

import lombok.Data;

@Data
public class LoginRequest {
    private String tag;
    private String password;
}
