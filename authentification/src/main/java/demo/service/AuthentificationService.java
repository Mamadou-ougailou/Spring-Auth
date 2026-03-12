package demo.service;

import demo.exception.UnauthorizedException;
import demo.model.Authority;
import demo.model.Identity;
import demo.model.Token;
import demo.repository.IdentityRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthentificationService {
    private final IdentityRepository identityRepository;
    private final IdentityService identityService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthentificationService(IdentityRepository identityRepository,
                                   IdentityService identityService,
                                   TokenService tokenService,
                                   PasswordEncoder passwordEncoder) {
        this.identityRepository = identityRepository;
        this.identityService = identityService;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    // Token validity: 24 hours (in milliseconds)
    private static final long TOKEN_VALIDITY = 24 * 60 * 60 * 1000;

    @Transactional
    public Token emailLogin(String email, String password) {
        Identity identity = identityService.findByEmail(email);
        Authority emailAuthority = identityService.findEmailAuthority(identity);
        identityService.ensureVerified(identity);

        if (!passwordEncoder.matches(password, emailAuthority.getSecret())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        long expirationTime = System.currentTimeMillis() + TOKEN_VALIDITY;
        Token token = new Token(identity, expirationTime);
        identity.addToken(token);
        identityRepository.save(identity);

        return token;
    }

    @Transactional
    public void logout(String tokenValue) {
        Token token = tokenService.findByValue(tokenValue);

        // Remove from the identity's token list (orphanRemoval will delete it)
        token.getIdentity().getTokens().remove(token);
        token.setIdentity(null);
    }

    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        Identity identity = identityService.findByEmail(email);
        Authority emailAuthority = identityService.findEmailAuthority(identity);
        identityService.ensureVerified(identity);

        if (!passwordEncoder.matches(oldPassword, emailAuthority.getSecret())) {
            throw new UnauthorizedException("Old password is incorrect");
        }

        emailAuthority.setSecret(passwordEncoder.encode(newPassword));
        identityRepository.save(identity);
    }

    /**
     * Get user info from a valid token.
     */
    @Transactional(readOnly = true)
    public Identity getUserInfo(String tokenValue) {
        Token token = tokenService.findValidToken(tokenValue);
        return token.getIdentity();
    }
}
