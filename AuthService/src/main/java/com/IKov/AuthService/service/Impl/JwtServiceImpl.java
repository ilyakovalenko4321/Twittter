package com.IKov.AuthService.service.Impl;

import com.IKov.AuthService.entity.Profile.Profile;
import com.IKov.AuthService.entity.Profile.Status;
import com.IKov.AuthService.entity.jwt.JwtTokenPair;
import com.IKov.AuthService.entity.jwt.JwtTokenType;
import com.IKov.AuthService.repository.ProfileRepository;
import com.IKov.AuthService.service.JwtService;
import com.IKov.AuthService.service.props.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {


    private final JwtProperties jwtProperties;
    private Key key;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AuthService authService;

    @PostConstruct
    private void init() {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes());
    }

    @Override
    public JwtTokenPair login(String tag, String password) {
        Profile profile = profileRepository.findByTag(tag);
        String realHashedPassword = profile.getPassword();
        boolean passwordMatches = passwordEncoder.matches(password, realHashedPassword);
        if (!passwordMatches) {
            return null;
        }
        return formJwtTokenPair(tag, profile);
    }

    @Override
    public JwtTokenPair renew(String token, String tag) {
        boolean isTokenValid = authService.validateRefreshToken(token, tag);

        if(!isTokenValid){
            return null;
        }

        Profile profile = profileRepository.findByTag(tag);
        redisTemplate.delete(token);
        return formJwtTokenPair(tag, profile);
    }

    @Override
    public boolean logout(String accessToken, String refreshToken) {
        redisTemplate.delete(accessToken);
        redisTemplate.delete(refreshToken);
        return true;
    }

    private JwtTokenPair formJwtTokenPair(String tag, Profile profile) {
        JwtTokenPair jwtTokenPair = new JwtTokenPair();
        jwtTokenPair.setAccessToken(generateToken(tag, profile.getStatus(), jwtProperties.getIssuer(), jwtProperties.getAccessTokenExpirationTime(), jwtProperties.getTimeUnits(), JwtTokenType.ACCESS));
        jwtTokenPair.setRefreshToken(generateToken(tag, profile.getStatus(), jwtProperties.getIssuer(), jwtProperties.getRefreshTokenExpirationTime(), jwtProperties.getTimeUnits(), JwtTokenType.REFRESH));

        redisTemplate.opsForValue().set(jwtTokenPair.getAccessToken(), tag);
        redisTemplate.expire(jwtTokenPair.getAccessToken(), jwtProperties.getAccessTokenExpirationTime(), jwtProperties.getTimeUnits());

        redisTemplate.opsForValue().set(jwtTokenPair.getRefreshToken(), tag);
        redisTemplate.expire(jwtTokenPair.getRefreshToken(), jwtProperties.getRefreshTokenExpirationTime(), jwtProperties.getTimeUnits());

        return jwtTokenPair;
    }

    private String generateToken(String tag, Status status, String issuer, Integer expirationTime, TimeUnit timeUnit, JwtTokenType type) {

        long expirationMillis = new Date().getTime() + timeUnit.toMillis(expirationTime);
        return Jwts.builder()
                .subject(tag)
                .claim("status", status.name())
                .claim("type", type.name())
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(new Date(expirationMillis))
                .signWith(key)
                .compact();

    }
}
