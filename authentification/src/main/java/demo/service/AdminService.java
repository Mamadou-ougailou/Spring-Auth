package demo.service;

import demo.dto.AdminIdentityResponse;
import demo.model.*;
import demo.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final CredentialRepository credentialRepository;
    private final IdentityRepository identityRepository;
    private final AuthorityRepository authorityRepository;
    private final TokenRepository tokenRepository;
    private final IdentityService identityService;
    private final TokenService tokenService;
    private final AuthentificationService authentificationService;

    public AdminService(CredentialRepository credentialRepository, IdentityRepository identityRepository,
                        AuthorityRepository authorityRepository, TokenRepository tokenRepository,
                        IdentityService identityService, TokenService tokenService,
                        AuthentificationService authentificationService) {
        this.credentialRepository = credentialRepository;
        this.identityRepository = identityRepository;
        this.authorityRepository = authorityRepository;
        this.tokenRepository = tokenRepository;
        this.identityService = identityService;
        this.tokenService = tokenService;
        this.authentificationService = authentificationService;
    }

    @Transactional
    public void deleteUser(String token, String email) {
        authentificationService.validateAdminToken(token);
        Identity identity = identityService.findByEmail(email);
        identityRepository.delete(identity);
    }

    @Transactional
    public void addCredential(String token, String name){
        authentificationService.validateAdminToken(token);
        if(credentialRepository.findByName(name).isPresent()){
            throw new RuntimeException("Credential with name '" + name + "' already exists");
        }
        Credential credential = new Credential(name);
        credentialRepository.save(credential);
    }
    
    @Transactional
    public void addRoleToUser(String token, String email, String roleName) {
        authentificationService.validateAdminToken(token);
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

    @Transactional
    public void removeRoleFromUser(String token, String email, String roleName) {
        authentificationService.validateAdminToken(token);
        Identity identity = identityService.findByEmail(email);

        Credential credentialToRemove = identity.getCredentials().stream()
                .filter(c -> c.getName().equals(roleName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User does not have role: " + roleName));

        identity.getCredentials().remove(credentialToRemove);
        identityRepository.save(identity);
    }

    @Transactional
    public void addAuthorityToUser(String token, String email, Authority.Provider provider, String secret) {
        authentificationService.validateAdminToken(token);
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

    @Transactional
    public void removeAuthorityFromUser(String token, String email, Authority.Provider provider) {
        authentificationService.validateAdminToken(token);
        Identity identity = identityService.findByEmail(email);

        Authority authToRemove = identity.getAuthorities().stream()
                .filter(a -> a.getProvider().equals(provider))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User has no authority for provider: " + provider));

        identity.removeAuthority(authToRemove);
        identityRepository.save(identity);
    }

    @Transactional
    public void deleteToken(String token, String tokenToDelete) {
        authentificationService.validateAdminToken(token);
        Token tokenEntity = tokenService.findByValue(tokenToDelete);
        tokenRepository.delete(tokenEntity);
    }

    // --- READ OPERATIONS ---

    @Transactional(readOnly = true)
    public List<AdminIdentityResponse> getAllUsers(String token) {
        authentificationService.validateAdminToken(token);
        return identityRepository.findAll().stream()
                .map(this::toAdminIdentityResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Set<AdminIdentityResponse> getAllConnectedUsers(String token) {
        authentificationService.validateAdminToken(token);
        long now = System.currentTimeMillis();
        return tokenRepository.findAllIdentitiesWithValidToken(now).stream()
                .map(this::toAdminIdentityResponse)
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public List<Credential> getAllCredentials(String token) {
        authentificationService.validateAdminToken(token);
        return credentialRepository.findAll();
    }

    @Transactional(readOnly = true) 
    public List<Authority> getAllAuthorities(String token) {
        authentificationService.validateAdminToken(token);
        return authorityRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Token> getAllTokens(String token) {
        authentificationService.validateAdminToken(token);
        return tokenRepository.findAll();
    }

    private AdminIdentityResponse toAdminIdentityResponse(Identity identity) {
        List<String> roles = identity.getCredentials().stream()
                .map(Credential::getName)
                .filter(role -> role != null && !role.isBlank())
                .sorted(Comparator.naturalOrder())
                .toList();

        return new AdminIdentityResponse(
                identity.getId(),
                identity.getEmail(),
                identity.getName(),
                identity.isVerified(),
                roles);
    }
}