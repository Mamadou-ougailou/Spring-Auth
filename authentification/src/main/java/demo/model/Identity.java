package demo.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "identities")
public class Identity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean verified = false;

    // --- 1. TOKENS (Session/JWT) ---
    // Owner: Token entity (via 'identity' field)
    @OneToMany(mappedBy = "identity", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("identity-tokens")
    private List<Token> tokens = new ArrayList<>();

    // --- 2. CREDENTIALS (Permissions/Roles) ---
    // Owner: Identity (This creates a join table 'identity_credentials')
    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "identity_credentials", joinColumns = @JoinColumn(name = "identity_id"), inverseJoinColumns = @JoinColumn(name = "credential_id"))
    private List<Credential> credentials = new ArrayList<>();

    // --- 3. AUTHORITIES (Login Methods like Password, Google) ---
    // Owner: Authority entity (via 'identity' field)
    @OneToMany(mappedBy = "identity", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("identity-authorities")
    private List<Authority> authorities = new ArrayList<>();

    public Identity() {
    }

    public Identity(String email, String name) {
        this.email = email;
        this.name = name;
    }

    public void addToken(Token token) {
        this.tokens.add(token);
        token.setIdentity(this); // REQUIRED: Sets the FK
    }

    public void removeToken(Token token) {
        this.tokens.remove(token);
        token.setIdentity(null);
    }

    public void addAuthority(Authority authority) {
        this.authorities.add(authority);
        authority.setIdentity(this); // REQUIRED: Sets the FK
    }

    public void removeAuthority(Authority authority) {
        this.authorities.remove(authority);
        authority.setIdentity(null);
    }

    // --- GETTERS & SETTERS ---

    public List<Token> getTokens() {
        return tokens;
    }

    public List<Authority> getAuthorities() {
        return authorities;
    }

    public List<Credential> getCredentials() {
        return credentials;
    }

    public void addCredential(Credential credential) {
        this.credentials.add(credential);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}