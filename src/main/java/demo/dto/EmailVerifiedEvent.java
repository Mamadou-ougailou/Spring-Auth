package demo.dto;

import java.time.LocalDateTime;


public class EmailVerifiedEvent {
    private String eventId;
    private String userId;
    private LocalDateTime occurredAt;
    private EventHeaders headers;
    // Constructors, Getters, Setters
}