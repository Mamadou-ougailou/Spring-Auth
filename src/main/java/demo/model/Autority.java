package demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tokens")
public class Autority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String token;

    @Column(nullable = false)
    private long expirationTime;

    @Column(nullable = false)
    private String email;

    public Autority() {
    }

    public Autority(String token, long expirationTime, String email) {
        this.token = token;
        this.expirationTime = expirationTime;
        this.email = email;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expirationTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}