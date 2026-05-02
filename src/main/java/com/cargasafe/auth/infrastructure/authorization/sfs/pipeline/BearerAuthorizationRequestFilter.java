package com.cargasafe.auth.infrastructure.authorization.sfs.pipeline;

import com.cargasafe.auth.infrastructure.authorization.sfs.model.UsernamePasswordAuthenticationTokenBuilder;
import com.cargasafe.auth.infrastructure.persistence.jpa.repositories.RevokedTokenRepository;
import com.cargasafe.auth.infrastructure.tokens.jwt.BearerTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BearerAuthorizationRequestFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BearerAuthorizationRequestFilter.class);

    private final BearerTokenService tokenService;
    private final RevokedTokenRepository revokedTokenRepository;
    private final UserDetailsService userDetailsService;

    public BearerAuthorizationRequestFilter(BearerTokenService tokenService,
                                            RevokedTokenRepository revokedTokenRepository,
                                            UserDetailsService userDetailsService) {
        this.tokenService = tokenService;
        this.revokedTokenRepository = revokedTokenRepository;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                String token = tokenService.getBearerTokenFrom(request);
                if (token != null && tokenService.validateToken(token) && !tokenService.isRefreshToken(token)) {
                    String jtiHash = sha256Hex(tokenService.getJti(token));
                    if (revokedTokenRepository.existsByJtiHash(jtiHash)) {
                        LOGGER.info("Access token revocado.");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                    String email = tokenService.getUsernameFromToken(token);
                    var userDetails = userDetailsService.loadUserByUsername(email);
                    SecurityContextHolder.getContext().setAuthentication(
                            UsernamePasswordAuthenticationTokenBuilder.build(userDetails, request)
                    );
                }
            }
        } catch (Exception e) {
            LOGGER.error("Authentication could not be established: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not supported", e);
        }
    }
}
