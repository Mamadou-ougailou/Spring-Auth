package demo.service;

import demo.exception.NotFoundException;
import demo.exception.UnauthorizedException;
import demo.model.Token;
import demo.repository.TokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * Centralised read/guard helpers for {@link Token}.
 * Used by AuthentificationService and AdminService.
 */
@Service
@Transactional(readOnly = true)
public class TokenService {

    private final TokenRepository tokenRepository;

    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /** Find a token by its raw value or throw 404. */
    public Token findByValue(String tokenValue) {
        return tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new NotFoundException("Token not found"));
    }

    /** Find a token and ensure it has not expired (throws 401 if expired). */
    public Token findValidToken(String tokenValue) {
        Token token = findByValue(tokenValue);
        if (token.isExpired()) {
            throw new UnauthorizedException("Token has expired");
        }
        return token;
    }
}
