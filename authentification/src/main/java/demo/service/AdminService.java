package demo.service;

import demo.model.*;
import demo.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class AdminService {
    @Autowired
    private CredentialRepository credentialRepository;
    @Autowired
    private IdentityRepository identityRepository;
    @Autowired
    private AuthorityRepository authorityRepository;
    @Autowired
    private TokenRepository tokenRepository;

    public void deleteUser(String email) {
        Identity identity = identityRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        identityRepository.delete(identity);
    }

    public void addRoleToUser(String email, String roleName) {
        Identity identity = identityRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Credential credential = credentialRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role '" + roleName + "' not found"));

        if (!(identity.isVerified())) {
            throw new RuntimeException("Cannot assign roles to unverified user");
        }
        // 3. Logic Fix: Check by NAME, not by Object Identity
        boolean hasRole = identity.getCredentials().stream()
                .anyMatch(c -> c.getName().equals(roleName));

        if (hasRole) {
            throw new RuntimeException("User already has this role");
        }

        identity.addCredential(credential);
        identityRepository.save(identity);
    }

    public void removeRoleFromUser(String email, String roleName) {
        Identity identity = identityRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Credential credentialToRemove = identity.getCredentials().stream()
                .filter(c -> c.getName().equals(roleName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User does not have role: " + roleName));

        identity.getCredentials().remove(credentialToRemove);
        identityRepository.save(identity);
    }

    // Assuming Authority.Provider is an Enum you defined, or a String
    public void addAuthorityToUser(String email, Authority.Provider provider, String secret) {
        Identity identity = identityRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!(identity.isVerified())) {
            throw new RuntimeException("Cannot assign authorities to unverified user");
        }

        // 4. Logic Fix: Check if user already has an authority of this PROVIDER type
        boolean exists = identity.getAuthorities().stream()
                .anyMatch(a -> a.getProvider().equals(provider));

        if (exists) {
            throw new RuntimeException("User already has an authority for provider: " + provider);
        }

        Authority authority = new Authority(provider, secret);
        identity.addAuthority(authority); // Helper handles the bi-directional link
        identityRepository.save(identity);
    }

    public void removeAuthorityFromUser(String email, Authority.Provider provider) {
        Identity identity = identityRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Authority authToRemove = identity.getAuthorities().stream()
                .filter(a -> a.getProvider().equals(provider))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User has no authority for provider: " + provider));

        identity.removeAuthority(authToRemove); // Helper handles setting null
        identityRepository.save(identity);
    }

    public void deleteToken(String token) {
        // Optimized: standard delete is fine, but handling "not found" is nice
        Token tokenEntity = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token not found"));
        tokenRepository.delete(tokenEntity);
    }

    // --- READ OPERATIONS ---

    @Transactional(readOnly = true) // Performance optimization for reads
    public List<Identity> getAllUsers() {
        return identityRepository.findAll();
    }

    /**
     * Optimized: Instead of Java filtering, we let the Database do the work.
     * This requires a custom query in TokenRepository (see below).
     */
    @Transactional(readOnly = true)
    public Set<Identity> getAllConnectedUsers() {
        // We use Set to avoid duplicates (e.g., user connected on Phone AND Laptop)
        long now = System.currentTimeMillis();
        return tokenRepository.findAllIdentitiesWithValidToken(now);
    }

    public List<Credential> getAllCredentials() {
        return credentialRepository.findAll();
    }

    public List<Authority> getAllAuthorities() {
        return authorityRepository.findAll();
    }

    public List<Token> getAllTokens() {
        return tokenRepository.findAll();
    }
}