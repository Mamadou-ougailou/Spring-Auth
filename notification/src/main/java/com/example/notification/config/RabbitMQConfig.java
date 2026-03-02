package com.example.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.mq.exchange}")
    private String exchange;

    @Value("${app.mq.queue.userRegistered}")
    private String userRegisteredQueue;

    @Value("${app.mq.rk.userRegistered}")
    private String userRegisteredRoutingKey;

    /* ===================== Main exchange ===================== */

    @Bean
    public TopicExchange authEventsExchange() {
        return new TopicExchange(exchange);
    }

    /* ===================== DLX / DLQ ===================== */

    @Bean
    public FanoutExchange deadLetterExchange() {
        return new FanoutExchange(exchange + ".dlx");
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(exchange + ".dlq").build();
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange());
    }

    /* ============ notification.user-registered queue ============ */

    @Bean
    public Queue userRegisteredQueue() {
        return QueueBuilder.durable(userRegisteredQueue)
                .withArgument("x-dead-letter-exchange", exchange + ".dlx")
                .build();
    }

    @Bean
    public Binding userRegisteredBinding() {
        return BindingBuilder.bind(userRegisteredQueue())
                .to(authEventsExchange())
                .with(userRegisteredRoutingKey);
    }

    /* ===================== JSON converter ===================== */

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
