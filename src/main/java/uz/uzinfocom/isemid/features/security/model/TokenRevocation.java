package uz.uzinfocom.isemid.features.security.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "token_revocations")
public class TokenRevocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * SHA-256 hex of the raw access token string
     */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    /**
     * Optional JWT ID if token contains "jti"
     */
    @Column(name = "jti", length = 128)
    private String jti;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at", nullable = false)
    private Instant revokedAt;

    @PrePersist
    void prePersist() {
        if (revokedAt == null) revokedAt = Instant.now();
    }
}
