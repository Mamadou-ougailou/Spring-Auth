package demo.service;

import demo.exception.NotFoundException;
import demo.exception.UnauthorizedException;
import demo.model.Authority;
import demo.model.Identity;
import demo.model.Token;
import demo.repository.IdentityRepository;
import demo.repository.TokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthentificationService {
    private final IdentityRepository identityRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthentificationService(IdentityRepository identityRepository, TokenRepository tokenRepository, PasswordEncoder passwordEncoder) {
        this.identityRepository = identityRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Token validity: 24 hours (in milliseconds)
    private static final long TOKEN_VALIDITY = 24 * 60 * 60 * 1000;

    @Transactional
    public Token emailLogin(String email, String password) {
        // 1. Find the identity by email
        Identity identity = identityRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        // 2. Find the EMAIL authority for this identity
        Authority emailAuthority = identity.getAuthorities().stream()
                .filter(a -> a.getProvider() == Authority.Provider.EMAIL)
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("No email login configured for this user"));

        //3. Check if the user is verified (for registration flow)
        if (!identity.isVerified()) {
            throw new UnauthorizedException("Email not verified. Please check your inbox.");
        }
        // 4. Verify the password against the stored hashed secret
        if (!passwordEncoder.matches(password, emailAuthority.getSecret())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        // 4. Create and persist a new token
        long expirationTime = System.currentTimeMillis() + TOKEN_VALIDITY;
        Token token = new Token(identity, expirationTime);
        identity.addToken(token); // maintains bidirectional relationship
        identityRepository.save(identity); // cascades to save the token

        return token;
    }

    @Transactional
    public void logout(String tokenValue) {
        Token token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new NotFoundException("Token not found"));

        // Remove from the identity's token list (orphanRemoval will delete it)
        token.getIdentity().getTokens().remove(token);
        token.setIdentity(null);
    }

    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        // 1. Find the identity
        Identity identity = identityRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // 2. Find the EMAIL authority
        Authority emailAuthority = identity.getAuthorities().stream()
                .filter(a -> a.getProvider() == Authority.Provider.EMAIL)
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("No email login configured for this user"));
        //3. Check if the user is verified (for registration flow)
        if (!identity.isVerified()) {
            throw new UnauthorizedException("Email not verified. Please check your inbox.");
        }
        // 4. Verify the old password
        if (!passwordEncoder.matches(oldPassword, emailAuthority.getSecret())) {
            throw new UnauthorizedException("Old password is incorrect");
        }

        // 4. Update with the new hashed password
        emailAuthority.setSecret(passwordEncoder.encode(newPassword));
        identityRepository.save(identity);
    }

    /**
     * Get user info from a valid token.
     */
    @Transactional(readOnly = true)
    public Identity getUserInfo(String tokenValue) {
        Token token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new NotFoundException("Token not found"));

        if (token.isExpired()) {
            throw new UnauthorizedException("Token has expired");
        }

        return token.getIdentity();
    }
}
