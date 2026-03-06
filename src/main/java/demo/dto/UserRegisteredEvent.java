package demo.dto;

import java.time.LocalDateTime;

public class UserRegisteredEvent {
    private String eventId; // UUID
    private LocalDateTime occurredAt;
    private UserRegisteredData data;
    private EventHeaders headers;

    // Nested class
    public static class UserRegisteredData {
        private String userId;
        private String email;
        private String tokenId;
        private String tokenClear; // Inclus uniquement pour TP simple
        // Getters, Setters
    }

    // Constructors, Getters, Setters
}
