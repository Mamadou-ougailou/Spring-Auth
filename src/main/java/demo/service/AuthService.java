package demo.service;

import demo.model.Credential;
import demo.model.Autority;
import demo.model.Identity;
import demo.repository.CredentialRepository;
import demo.repository.IdentityRepository;
import demo.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private IdentityRepository identityRepository;

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Vérifier si un utilisateur existe
    public boolean userExists(String email) {
        return identityRepository.existsByEmail(email);
    }

    // Enregistrer un nouvel utilisateur
    @Transactional
    public Identity registerUser(String email, String name, String password) {
        if (userExists(email)) {
            return null;
        }

        Identity newIdentity = new Identity(email, name);

        // Hasher le mot de passe avant de le sauvegarder
        String hashedPassword = passwordEncoder.encode(password);
        Credential newCredential = new Credential(email, hashedPassword);

        identityRepository.save(newIdentity);
        credentialRepository.save(newCredential);

        return newIdentity;
    }

    // Vérifier le mot de passe
    public boolean verifyPassword(String email, String rawPassword) {
        Credential credential = credentialRepository.findByEmail(email).orElse(null);

        if (credential == null) {
            return false;
        }

        // Comparer le mot de passe fourni avec le hash stocké
        return passwordEncoder.matches(rawPassword, credential.getPassword());
    }

    // Obtenir le credential d'un utilisateur
    public Credential getCredential(String email) {
        return credentialRepository.findByEmail(email).orElse(null);
    }

    // Générer un nouveau token
    public Autority generateToken(String email) {
        String token = UUID.randomUUID().toString();
        long expirationTime = System.currentTimeMillis() + 3600000; // 1 heure

        Autority credentialToken = new Autority(token, expirationTime, email);
        return tokenRepository.save(credentialToken);
    }

    // Vérifier si un token est valide
    public boolean isTokenValid(String token) {
        Autority credentialToken = tokenRepository.findByToken(token).orElse(null);

        if (credentialToken == null) {
            return false;
        }

        if (credentialToken.isExpired()) {
            tokenRepository.delete(credentialToken);
            return false;
        }

        return true;
    }

    // Obtenir les informations d'un token
    public Autority getTokenInfo(String token) {
        return tokenRepository.findByToken(token).orElse(null);
    }

    // Révoquer un token
    @Transactional
    public void revokeToken(String token) {
        tokenRepository.findByToken(token).ifPresent(tokenRepository::delete);
    }

    // Récupérer tous les utilisateurs
    public Collection<Identity> getAllUsers() {
        return identityRepository.findAll();
    }

    // Récupérer tous les credentials
    public Collection<Credential> getAllCredentials() {
        return credentialRepository.findAll();
    }

    // Supprimer un utilisateur
    @Transactional
    public boolean deleteUser(String email) {
        if (!userExists(email)) {
            return false;
        }

        // Supprimer tous les tokens de l'utilisateur
        tokenRepository.deleteByEmail(email);

        // Supprimer le credential
        credentialRepository.deleteByEmail(email);

        // Supprimer l'identité
        identityRepository.deleteByEmail(email);

        return true;
    }
}
