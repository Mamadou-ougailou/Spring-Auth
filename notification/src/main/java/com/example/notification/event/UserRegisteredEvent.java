package com.example.notification.event;

import java.time.Instant;

/**
 * Mirror of the Auth service's UserRegisteredEvent.
 * Deserialized from the JSON message consumed from RabbitMQ.
 */
public class UserRegisteredEvent {

    private String eventId;
    private String userId;
    private String email;
    private String tokenId;
    private String tokenClear;
    private Instant occurredAt;

    public UserRegisteredEvent() {
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
