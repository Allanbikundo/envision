package com.bikundo.product.config;


import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakAdminConfig {
    
    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;
    
    @Value("${keycloak.realm}")
    private String realm;
    
    @Value("${keycloak.resource}")
    private String clientId;
    
    @Value("${keycloak.credentials.secret}")
    private String clientSecret;
    
    @Bean(name = "keycloakAdmin")
    public Keycloak keycloakAdmin() {
        return KeycloakBuilder.builder()
            .serverUrl(authServerUrl)
            .realm("master") // Use master realm for admin operations
            .clientId("admin-cli")
            .grantType(OAuth2Constants.PASSWORD)
            .username("admin") // Default Keycloak admin user
            .password("admin") // Default Keycloak admin password
            .build();
    }
}

