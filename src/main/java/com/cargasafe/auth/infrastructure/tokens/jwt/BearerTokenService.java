package com.cargasafe.auth.infrastructure.tokens.jwt;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface BearerTokenService {
    String generateToken(String username);
    String generateToken(String username, Long userId, List<String> roles);
    String allocateToken(Authentication authentication);
    String allocateToken(String subject);
    String allocateAccessToken(String userId, Map<String, Object> extraClaims);
    String allocateRefreshToken(String userId);
    String getBearerTokenFrom(HttpServletRequest request);
    String getUsernameFromToken(String token);
    boolean validateToken(String token);
    boolean isRefreshToken(String token);
    String getJti(String token);
    Date getExpiration(String token);
}
