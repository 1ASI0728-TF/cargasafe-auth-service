package com.cargasafe.auth.infrastructure.tokens.jwt.services;

import com.cargasafe.auth.infrastructure.tokens.jwt.BearerTokenService;
import com.cargasafe.auth.infrastructure.tokens.jwt.JwtProps;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Service
public class TokenServiceImpl implements BearerTokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenServiceImpl.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int TOKEN_BEGIN_INDEX = 7;

    private final SecretKey signingKey;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    public TokenServiceImpl(JwtProps jwtProps) {
        this.accessTtl = jwtProps.getAccessTtl();
        this.refreshTtl = jwtProps.getRefreshTtl();
        this.signingKey = Keys.hmacShaKeyFor(jwtProps.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateToken(String username) {
        return allocateAccessToken(username, Map.of());
    }

    @Override
    public String generateToken(String username, Long userId, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", userId);
        claims.put("roles", roles);
        return allocateAccessToken(username, claims);
    }

    @Override
    public String allocateToken(Authentication authentication) {
        return allocateAccessToken(authentication.getName(), Map.of());
    }

    @Override
    public String allocateToken(String subject) {
        return allocateAccessToken(subject, Map.of());
    }

    @Override
    public String allocateAccessToken(String userId, Map<String, Object> extraClaims) {
        var claims = new HashMap<String, Object>();
        claims.put("typ", "access");
        if (extraClaims != null) claims.putAll(extraClaims);
        return buildToken(userId, accessTtl, claims);
    }

    @Override
    public String allocateRefreshToken(String userId) {
        return buildToken(userId, refreshTtl, Map.of("typ", "refresh"));
    }

    @Override
    public boolean isRefreshToken(String token) {
        try {
            String typ = extractClaim(token, c -> c.get("typ", String.class));
            return "refresh".equalsIgnoreCase(typ);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    @Override
    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
            return true;
        } catch (SignatureException e) {
            LOGGER.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            LOGGER.warn("Invalid JWT: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            LOGGER.info("JWT expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            LOGGER.warn("JWT unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.warn("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public String getBearerTokenFrom(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(TOKEN_BEGIN_INDEX);
        }
        return null;
    }

    @Override
    public Date getExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private String buildToken(String userId, Duration ttl, Map<String, Object> claims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId)
                .claims(claims)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(signingKey)
                .compact();
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(parse(token));
    }
}
