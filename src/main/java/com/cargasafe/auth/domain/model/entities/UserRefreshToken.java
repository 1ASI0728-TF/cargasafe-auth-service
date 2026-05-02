package com.cargasafe.auth.domain.model.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "user_refresh_tokens",
        indexes = {
                @Index(name = "idx_user_refresh_tokens_user_id", columnList = "user_id"),
                @Index(name = "idx_user_refresh_tokens_jti_hash", columnList = "jti_hash", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "jti_hash", nullable = false, unique = true, length = 64)
    private String jtiHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;
}
