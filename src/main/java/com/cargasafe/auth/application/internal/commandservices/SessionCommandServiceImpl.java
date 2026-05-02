package com.cargasafe.auth.application.internal.commandservices;

import com.cargasafe.auth.domain.model.commands.*;
import com.cargasafe.auth.domain.model.entities.RevokedToken;
import com.cargasafe.auth.domain.model.entities.UserRefreshToken;
import com.cargasafe.auth.domain.model.exceptions.InvalidCredentialsException;
import com.cargasafe.auth.domain.model.queries.GetUserByEmailQuery;
import com.cargasafe.auth.domain.model.valueobjects.TokenPair;
import com.cargasafe.auth.domain.services.SessionCommandService;
import com.cargasafe.auth.domain.services.UserQueryService;
import com.cargasafe.auth.infrastructure.persistence.jpa.repositories.RevokedTokenRepository;
import com.cargasafe.auth.infrastructure.persistence.jpa.repositories.UserRefreshTokenRepository;
import com.cargasafe.auth.infrastructure.tokens.jwt.BearerTokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class SessionCommandServiceImpl implements SessionCommandService {

    private final BearerTokenService tokenService;
    private final UserQueryService userQueryService;
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final TokenRevocationCommandService tokenRevocationCommandService;

    public SessionCommandServiceImpl(BearerTokenService tokenService,
                                     UserQueryService userQueryService,
                                     UserRefreshTokenRepository userRefreshTokenRepository,
                                     RevokedTokenRepository revokedTokenRepository,
                                     TokenRevocationCommandService tokenRevocationCommandService) {
        this.tokenService = tokenService;
        this.userQueryService = userQueryService;
        this.userRefreshTokenRepository = userRefreshTokenRepository;
        this.revokedTokenRepository = revokedTokenRepository;
        this.tokenRevocationCommandService = tokenRevocationCommandService;
    }

    @Override
    public void handle(RegisterRefreshTokenCommand command) {
        String refreshToken = command.refreshToken();
        if (refreshToken == null || refreshToken.isBlank()) return;

        String jti = tokenService.getJti(refreshToken);
        if (jti == null || jti.isBlank()) return;

        String jtiHash = sha256Hex(jti);
        if (userRefreshTokenRepository.findByJtiHash(jtiHash).isPresent()) return;

        Instant expiresAt = tokenService.getExpiration(refreshToken).toInstant();
        userRefreshTokenRepository.save(new UserRefreshToken(null, command.userId(), jtiHash, expiresAt, false));
    }

    @Override
    public TokenPair handle(RefreshAccessTokenCommand command) {
        String refreshToken = command.refreshToken();
        if (refreshToken == null || refreshToken.isBlank()) throw new InvalidCredentialsException();
        if (!tokenService.validateToken(refreshToken)) throw new InvalidCredentialsException();
        if (!tokenService.isRefreshToken(refreshToken)) throw new InvalidCredentialsException();

        String jtiHash = sha256Hex(tokenService.getJti(refreshToken));
        var stored = userRefreshTokenRepository.findByJtiHash(jtiHash)
                .orElseThrow(InvalidCredentialsException::new);

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now()))
            throw new InvalidCredentialsException();

        String email = tokenService.getUsernameFromToken(refreshToken);
        var user = userQueryService.handle(new GetUserByEmailQuery(email))
                .orElseThrow(InvalidCredentialsException::new);

        String newAccess = tokenService.generateToken(user.getEmail(), user.getId(), user.getSerializedRoles());
        String newRefresh = tokenService.allocateRefreshToken(user.getEmail());

        stored.setRevoked(true);
        userRefreshTokenRepository.save(stored);
        tokenRevocationCommandService.handle(new RevokeTokenCommand(refreshToken));
        handle(new RegisterRefreshTokenCommand(user.getId(), newRefresh));

        return new TokenPair(newAccess, newRefresh);
    }

    @Override
    public void handle(LogoutAllDevicesCommand command) {
        Instant now = Instant.now();
        List<UserRefreshToken> active =
                userRefreshTokenRepository.findAllByUserIdAndRevokedFalseAndExpiresAtAfter(command.userId(), now);
        if (active.isEmpty()) return;

        for (UserRefreshToken t : active) {
            t.setRevoked(true);
            if (!revokedTokenRepository.existsByJtiHash(t.getJtiHash())) {
                revokedTokenRepository.save(new RevokedToken(t.getJtiHash(), t.getExpiresAt()));
            }
        }
        userRefreshTokenRepository.saveAll(active);
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
