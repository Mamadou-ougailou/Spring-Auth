package demo.service;

import demo.exception.NotFoundException;
import demo.exception.UnauthorizedException;
import demo.model.Authority;
import demo.model.Identity;
import demo.repository.IdentityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Centralised read/guard helpers for {@link Identity}.
 * Used by AuthentificationService, RegistrationService and AdminService.
 */

@Service
@Transactional(readOnly = true)
public class IdentityService {

    private final IdentityRepository identityRepository;

    public IdentityService(IdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    /** Find an identity by e-mail or throw 404. */
    public Identity findByEmail(String email) {
        return identityRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    /** Find the EMAIL authority attached to an identity, or throw 401. */
    public Authority findEmailAuthority(Identity identity) {
        return identity.getAuthorities().stream()
                .filter(a -> a.getProvider() == Authority.Provider.EMAIL)
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("No email login configured for this user"));
    }

    /** Throw 401 if the identity has not been verified yet. */
    public void ensureVerified(Identity identity) {
        if (!identity.isVerified()) {
            throw new UnauthorizedException("Email not verified. Please check your inbox.");
        }
    }
}
