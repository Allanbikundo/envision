package com.bikundo.order;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.UUID;

@TestConfiguration
public class TestSecurityConfig {
    @Bean
    public JwtDecoder jwtDecoder() {
        return token -> Jwt.withTokenValue(token)
                .claim("sub", UUID.randomUUID().toString())
                .header("alg", "none")
                .build();
    }
}