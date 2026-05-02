package com.cargasafe.auth.application.internal.commandservices;

import com.cargasafe.auth.domain.model.commands.RevokeTokenCommand;
import com.cargasafe.auth.domain.model.entities.RevokedToken;
import com.cargasafe.auth.infrastructure.persistence.jpa.repositories.RevokedTokenRepository;
import com.cargasafe.auth.infrastructure.tokens.jwt.BearerTokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@Transactional
public class TokenRevocationCommandService {

    private final RevokedTokenRepository revokedTokenRepository;
    private final BearerTokenService tokenService;

    public TokenRevocationCommandService(RevokedTokenRepository revokedTokenRepository,
                                         BearerTokenService tokenService) {
        this.revokedTokenRepository = revokedTokenRepository;
        this.tokenService = tokenService;
    }

    public void handle(RevokeTokenCommand command) {
        String token = command.token();
        if (token == null || token.isBlank()) return;

        String jti = tokenService.getJti(token);
        if (jti == null || jti.isBlank()) return;

        String jtiHash = sha256Hex(jti);
        if (revokedTokenRepository.existsByJtiHash(jtiHash)) return;

        revokedTokenRepository.save(new RevokedToken(jtiHash, tokenService.getExpiration(token).toInstant()));
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
