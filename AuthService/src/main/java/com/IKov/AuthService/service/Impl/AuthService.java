package com.IKov.AuthService.service.Impl;

import com.IKov.AuthService.entity.Profile.Status;
import com.IKov.AuthService.entity.jwt.JwtTokenType;
import com.IKov.AuthService.service.props.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    private Key signingKey;

    @PostConstruct
    public void init() {
        signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes());
    }


    public boolean validate(String token, String expectedTag) {

        token = token.substring(7);

        Claims claims = parseAndValidateSignature(token).orElse(null);
        if (claims == null) {
            return false;
        }

        String subject = claims.getSubject();
        if (!Objects.equals(subject, expectedTag)) {
            log.warn("Token subject '{}' не совпадает с ожидаемым '{}'", subject, expectedTag);
            return false;
        }

        if (isTokenInactive(token, expectedTag)) {
            log.warn("Token '{}' отсутствует в Redis или привязан не к '{}'", token, expectedTag);
            return false;
        }

        return isUserConfirmed(claims);
    }

    public boolean validateRefreshToken(String token, String tag){
        Claims claims = parseAndValidateSignature(token).orElse(null);

        if(claims == null){
            return false;
        }

        String subject = claims.getSubject();
        if (!Objects.equals(subject, tag)) {
            log.warn("Token subject '{}' не совпадает с ожидаемым '{}' refresh", subject, tag);
            return false;
        }

        if (isTokenInactive(token, tag)) {
            log.warn("Token '{}' отсутствует в Redis или привязан не к '{}' refresh", token, tag);
            return false;
        }

        JwtTokenType type = JwtTokenType.valueOf((String) claims.get("type"));
        if(type != JwtTokenType.REFRESH){
            log.warn("Токен не является REFRESH");
            return false;
        }

        return true;
    }

    private Optional<Claims> parseAndValidateSignature(String token) {
        try {
            Jws<Claims> jws = Jwts.parser()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);
            return Optional.of(jws.getBody());
        } catch (JwtException ex) {
            log.warn("Не удалось распарсить или проверить подпись JWT: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private boolean isTokenInactive(String token, String expectedTag) {
        Object storedTag = redisTemplate.opsForValue().get(token);
        return !expectedTag.equals(storedTag);
    }

    private boolean isUserConfirmed(Claims claims) {
        String statusName = claims.get("status", String.class);
        try {
            Status status = Status.valueOf(statusName);
            return status == Status.CONFIRMED;
        } catch (IllegalArgumentException | NullPointerException ex) {
            log.warn("Некорректный статус в JWT: {}", statusName);
            return false;
        }
    }
}
