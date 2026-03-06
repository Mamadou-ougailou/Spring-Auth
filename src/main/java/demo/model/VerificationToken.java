package demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;




@Entity
@Table(name = "verification_tokens")
public class VerificationToken {
    @Id
    @Column(nullable = false, unique = true)
    private String tokenId;  // UUID public
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private String tokenHash;  // BCrypt hash du tokenClear
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    // Constructors, Getters, Setters
}