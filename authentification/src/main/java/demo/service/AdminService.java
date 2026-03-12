package demo.service;

import demo.model.*;
import demo.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class AdminService {

    private final CredentialRepository credentialRepository;
    private final IdentityRepository identityRepository;
    private final AuthorityRepository authorityRepository;
    private final TokenRepository tokenRepository;
    private final IdentityService identityService;
    private final TokenService tokenService;

    public AdminService(CredentialRepository credentialRepository, IdentityRepository identityRepository,
                        AuthorityRepository authorityRepository, TokenRepository tokenRepository,
                        IdentityService identityService, TokenService tokenService) {
        this.credentialRepository = credentialRepository;
        this.identityRepository = identityRepository;
        this.authorityRepository = authorityRepository;
        this.tokenRepository = tokenRepository;
        this.identityService = identityService;
        this.tokenService = tokenService;
    }

    public void deleteUser(String email) {
        Identity identity = identityService.findByEmail(email);
        identityRepository.delete(identity);
    }

    public void addCredential(String name){
        if(credentialRepository.findByName(name).isPresent()){
            throw new RuntimeException("Credential with name '" + name + "' already exists");
        }
        Credential credential = new Credential(name);
        credentialRepository.save(credential);
    }
    
    public void addRoleToUser(String email, String roleName) {
        Identity identity = identityService.findByEmail(email);

        Credential credential = credentialRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role '" + roleName + "' not found"));

        identityService.ensureVerified(identity);

        boolean hasRole = identity.getCredentials().stream()
                .anyMatch(c -> c.getName().equals(roleName));

        if (hasRole) {
            throw new RuntimeException("User already has this role");
        }

        identity.addCredential(credential);
        identityRepository.save(identity);
    }

    public void removeRoleFromUser(String email, String roleName) {
        Identity identity = identityService.findByEmail(email);

        Credential credentialToRemove = identity.getCredentials().stream()
                .filter(c -> c.getName().equals(roleName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User does not have role: " + roleName));

        identity.getCredentials().remove(credentialToRemove);
        identityRepository.save(identity);
    }

    public void addAuthorityToUser(String email, Authority.Provider provider, String secret) {
        Identity identity = identityService.findByEmail(email);
        identityService.ensureVerified(identity);

        boolean exists = identity.getAuthorities().stream()
                .anyMatch(a -> a.getProvider().equals(provider));

        if (exists) {
            throw new RuntimeException("User already has an authority for provider: " + provider);
        }

        Authority authority = new Authority(provider, secret);
        identity.addAuthority(authority);
        identityRepository.save(identity);
    }

    public void removeAuthorityFromUser(String email, Authority.Provider provider) {
        Identity identity = identityService.findByEmail(email);

        Authority authToRemove = identity.getAuthorities().stream()
                .filter(a -> a.getProvider().equals(provider))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User has no authority for provider: " + provider));

        identity.removeAuthority(authToRemove);
        identityRepository.save(identity);
    }

    public void deleteToken(String token) {
        Token tokenEntity = tokenService.findByValue(token);
        tokenRepository.delete(tokenEntity);
    }

    // --- READ OPERATIONS ---

    @Transactional(readOnly = true)
    public List<Identity> getAllUsers() {
        return identityRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Set<Identity> getAllConnectedUsers() {
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