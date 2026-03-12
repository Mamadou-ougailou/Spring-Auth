package demo.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "authorities")
public class Authority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // switch the provider to an Enum
    public enum Provider {
        EMAIL,
        GOOGLE,
        GITHUB,
        MOODLE
    }

    @Column(unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private Provider provider; // provider of the connection (e.g., "email", "google", "github")
    @Column(nullable = false)
    @JsonIgnore
    private String secret; // password for email provider ...

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "identity_id", nullable = false)
    @JsonBackReference("identity-authorities")
    private Identity identity;

    public Authority() {
    }

    public Authority(Provider provider, String secret) {
        this.provider = provider;
        this.secret = secret;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }
}