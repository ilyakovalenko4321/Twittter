package com.IKov.AuthService.service.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

@Data
@ConfigurationProperties(prefix = "configuration.jwt")
public class JwtProperties {

    private String secretKey;
    private String issuer;
    private Integer accessTokenExpirationTime;
    private Integer refreshTokenExpirationTime;
    private TimeUnit timeUnits;

    public void setTimeUnits(String timeUnits) {
        this.timeUnits = TimeUnit.valueOf(timeUnits);
    }
}
