package demo.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "verification_tokens")
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Public identifier sent in the verification link (UUID). */
    @Column(unique = true, nullable = false)
    private String tokenId;

    /** BCrypt hash of the raw token — never store the clear-text token. */
    @Column(nullable = false)
    private String tokenHash;

    @Column(nullable = false)
    private Instant expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "identity_id", nullable = false)
    private Identity identity;

    public VerificationToken() {
    }

    public VerificationToken(String tokenId, String tokenHash, Instant expiresAt, Identity identity) {
        this.tokenId = tokenId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.identity = identity;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    // --- getters / setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTokenId() { return tokenId; }
    public void setTokenId(String tokenId) { this.tokenId = tokenId; }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Identity getIdentity() { return identity; }
    public void setIdentity(Identity identity) { this.identity = identity; }
}
