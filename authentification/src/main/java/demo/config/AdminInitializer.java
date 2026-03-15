package demo.config;

import demo.model.Authority;
import demo.model.Credential;
import demo.model.Identity;
import demo.repository.AuthorityRepository;
import demo.repository.CredentialRepository;
import demo.repository.IdentityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final CredentialRepository credentialRepository;
    private final IdentityRepository identityRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(CredentialRepository credentialRepository,
                            IdentityRepository identityRepository,
                            AuthorityRepository authorityRepository,
                            PasswordEncoder passwordEncoder) {
        this.credentialRepository = credentialRepository;
        this.identityRepository = identityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        String adminEmail = System.getenv("ADMIN_EMAIL");
        String adminPassword = System.getenv("ADMIN_PASSWORD");

        if (adminEmail == null || adminEmail.isBlank()) {
            log.info("ADMIN_EMAIL not set — skipping admin initialization");
            return;
        }

        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn("ADMIN_PASSWORD not set — admin will be created without a password");
        }

        // Ensure credential 'admin' exists
        Credential adminCred = credentialRepository.findByName("admin")
                .orElseGet(() -> {
                    log.info("Creating credential 'admin'");
                    return credentialRepository.save(new Credential("admin"));
                });

        // Ensure identity exists
        Identity identity = identityRepository.findByEmail(adminEmail).orElseGet(() -> {
            log.info("Creating admin identity: {}", adminEmail);
            Identity id = new Identity(adminEmail, "Administrator");
            id.setVerified(true);
            return identityRepository.save(id);
        });

        // Assign admin credential if missing
        boolean hasAdmin = identity.getCredentials().stream()
                .anyMatch(c -> c.getName() != null && c.getName().equalsIgnoreCase("admin"));
        if (!hasAdmin) {
            log.info("Assigning 'admin' credential to {}", adminEmail);
            identity.addCredential(adminCred);
        }

        // Ensure EMAIL authority exists
        boolean hasEmailAuth = identity.getAuthorities().stream()
                .anyMatch(a -> a.getProvider() == Authority.Provider.EMAIL);
        if (!hasEmailAuth) {
            String secret = adminPassword == null ? "" : passwordEncoder.encode(adminPassword);
            log.info("Adding EMAIL authority to admin identity");
            Authority authority = new Authority(Authority.Provider.EMAIL, secret);
            identity.addAuthority(authority);
        }

        identityRepository.save(identity);
        log.info("Admin initialization complete for {}", adminEmail);
    }
}
