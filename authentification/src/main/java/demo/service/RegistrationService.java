package demo.service;

import demo.event.UserRegisteredEvent;
import demo.exception.BadRequestException;
import demo.exception.ConflictException;
import demo.exception.NotFoundException;
import demo.model.Authority;
import demo.model.Identity;
import demo.model.VerificationToken;
import demo.repository.IdentityRepository;
import demo.repository.VerificationTokenRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Handles user registration and e-mail verification flow.
 */
@Service
public class RegistrationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);

    /** Verification token time-to-live (30 minutes). */
    private static final Duration TOKEN_TTL = Duration.ofMinutes(30);

    @Autowired
    private IdentityRepository identityRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${app.mq.exchange}")
    private String exchange;

    @Value("${app.mq.rk.userRegistered}")
    private String userRegisteredRoutingKey;

    /* ================================================================
     *  POST /register
     * ================================================================ */

    @Transactional
    public Identity register(String email, String password) {
        if (identityRepository.existsByEmail(email)) {
            throw new ConflictException("A user with this e-mail already exists");
        }

        // 1. Create Identity (verified = false)
        String name = email.split("@")[0]; // derive a display name from the e-mail
        Identity identity = new Identity(email, name);
        identity.setVerified(false);

        // 2. Attach EMAIL authority (hashed password)
        String hashedPassword = passwordEncoder.encode(password);
        identity.addAuthority(new Authority(Authority.Provider.EMAIL, hashedPassword));
        identity = identityRepository.save(identity);

        // 3. Generate verification token
        String tokenId = UUID.randomUUID().toString();
        String tokenClear = UUID.randomUUID().toString();
        String tokenHash = passwordEncoder.encode(tokenClear);
        Instant expiresAt = Instant.now().plus(TOKEN_TTL);

        VerificationToken vt = new VerificationToken(tokenId, tokenHash, expiresAt, identity);
        verificationTokenRepository.save(vt);

        // 4. Publish UserRegistered event to RabbitMQ
        UserRegisteredEvent event = new UserRegisteredEvent(
                identity.getId().toString(),
                identity.getEmail(),
                tokenId,
                tokenClear
        );

        rabbitTemplate.convertAndSend(exchange, userRegisteredRoutingKey, event, message -> {
            message.getMessageProperties().setHeader("x-correlation-id", event.getEventId());
            message.getMessageProperties().setHeader("x-schema-version", 1);
            return message;
        });

        log.info("[Register] User '{}' created (id={}). UserRegistered event published (eventId={}).",
                email, identity.getId(), event.getEventId());

        return identity;
    }

    /* ================================================================
     *  GET /verify?tokenId=...&t=...
     * ================================================================ */

    @Transactional
    public void verify(String tokenId, String rawToken) {
        VerificationToken vt = verificationTokenRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new NotFoundException("Invalid or already-used verification token"));

        // Already verified? → idempotent
        if (vt.getIdentity().isVerified()) {
            log.info("[Verify] Account already verified for tokenId={}.", tokenId);
            return;
        }

        // Expired?
        if (vt.isExpired()) {
            throw new BadRequestException("Verification token has expired");
        }

        // Compare BCrypt(rawToken) with stored hash
        if (!passwordEncoder.matches(rawToken, vt.getTokenHash())) {
            throw new BadRequestException("Invalid verification token");
        }

        // Mark account as verified
        Identity identity = vt.getIdentity();
        identity.setVerified(true);
        identityRepository.save(identity);

        // Delete verification token (one-shot)
        verificationTokenRepository.delete(vt);

        log.info("[Verify] Account verified for user '{}' (id={}).", identity.getEmail(), identity.getId());
    }
}
