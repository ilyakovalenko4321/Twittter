package com.IKov.AuthService.web.controller;

import com.IKov.AuthService.entity.jwt.JwtTokenPair;
import com.IKov.AuthService.entity.jwt.LoginRequest;
import com.IKov.AuthService.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tokens")
public class JwtController {

    private final JwtService jwtService;

    @PostMapping("/public/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            JwtTokenPair jwtTokenPair = jwtService.login(loginRequest.getTag(), loginRequest.getPassword());
            if (jwtTokenPair == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid tag or password");
            }
            return ResponseEntity.ok(jwtTokenPair);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Login failed: " + e.getMessage());
        }
    }

    @PostMapping("/public/renew")
    public ResponseEntity<?> renew(@RequestParam String token, @RequestParam String tag) {
        try {
            JwtTokenPair jwtTokenPair = jwtService.renew(token, tag);
            if (jwtTokenPair == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid or expired refresh token");
            }
            return ResponseEntity.ok(jwtTokenPair);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Token renewal failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/public/logout")
    public ResponseEntity<?> logout(@RequestParam String accessToken,
                                    @RequestParam String refreshToken) {
        try {
            boolean success = jwtService.logout(accessToken, refreshToken);
            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid tokens or already logged out");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Logout failed: " + e.getMessage());
        }
    }

}
