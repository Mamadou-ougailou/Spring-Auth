package com.example.notification.consumer;

import com.example.notification.event.UserRegisteredEvent;
import com.example.notification.service.EmailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumes {@code UserRegistered} events from the
 * {@code notification.user-registered} queue and triggers the
 * verification e-mail.
 */
@Component
public class UserRegisteredConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserRegisteredConsumer.class);

    private final EmailService emailService;

    public UserRegisteredConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = "${app.mq.queue.userRegistered}")
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("[Consumer] Received UserRegistered event (eventId={}, userId={}, email={}).",
                event.getEventId(), event.getUserId(), event.getEmail());

        emailService.sendVerificationEmail(
                event.getEmail(),
                event.getTokenId(),
                event.getTokenClear());
    }
}
