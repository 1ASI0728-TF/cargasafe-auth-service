package com.cargasafe.auth.infrastructure.persistence.jpa.repositories;

import com.cargasafe.auth.domain.model.entities.UserRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserRefreshTokenRepository extends JpaRepository<UserRefreshToken, Long> {
    Optional<UserRefreshToken> findByJtiHash(String jtiHash);
    List<UserRefreshToken> findAllByUserIdAndRevokedFalseAndExpiresAtAfter(Long userId, Instant now);
}
