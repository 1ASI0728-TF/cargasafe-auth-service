package com.cargasafe.auth.infrastructure.persistence.jpa.repositories;

import com.cargasafe.auth.domain.model.entities.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {
    boolean existsByJtiHash(String jtiHash);
}
