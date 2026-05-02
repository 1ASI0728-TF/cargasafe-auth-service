package com.cargasafe.auth.infrastructure.tokens.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "authorization.jwt")
public class JwtProps {
    private String secret;
    private Duration accessTtl;
    private Duration refreshTtl;
}
