package com.cargasafe.auth.domain.model.entities;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "revoked_tokens",
        indexes = {
                @Index(name = "idx_revoked_tokens_jti_hash", columnList = "jti_hash", unique = true)
        }
)
public class RevokedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jti_hash", nullable = false, unique = true, length = 64)
    private String jtiHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public RevokedToken() {}

    public RevokedToken(String jtiHash, Instant expiresAt) {
        this.jtiHash = jtiHash;
        this.expiresAt = expiresAt;
    }
}
