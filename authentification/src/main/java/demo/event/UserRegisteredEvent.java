package demo.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published to RabbitMQ when a new user registers.
 * Consumed by the Notification service to send the verification e-mail.
 */
public class UserRegisteredEvent {

    private String eventId;
    private String userId;
    private String email;
    private String tokenId;
    /** Raw token (clear-text) — included so Notification can build the verification link. */
    private String tokenClear;
    private Instant occurredAt;

    public UserRegisteredEvent() {
    }

    public UserRegisteredEvent(String userId, String email, String tokenId, String tokenClear) {
        this.eventId = UUID.randomUUID().toString();
        this.userId = userId;
        this.email = email;
        this.tokenId = tokenId;
        this.tokenClear = tokenClear;
        this.occurredAt = Instant.now();
    }

    // --- getters / setters ---

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTokenId() { return tokenId; }
    public void setTokenId(String tokenId) { this.tokenId = tokenId; }

    public String getTokenClear() { return tokenClear; }
    public void setTokenClear(String tokenClear) { this.tokenClear = tokenClear; }

    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
}
