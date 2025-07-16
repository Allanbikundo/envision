package com.bikundo.product.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableRabbit
public class RabbitMQConfig {

    // Constants (good practice you already have)
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_PLACED_QUEUE = "order.placed.queue";
    public static final String ORDER_PLACED_ROUTING_KEY = "order.placed";

    public static final String INVENTORY_EXCHANGE = "inventory.exchange";
    public static final String INVENTORY_RESERVED_QUEUE = "inventory.reserved.queue";
    public static final String INVENTORY_FAILED_QUEUE = "inventory.failed.queue";
    public static final String INVENTORY_RESERVED_ROUTING_KEY = "inventory.reserved";
    public static final String INVENTORY_FAILED_ROUTING_KEY = "inventory.failed";

    public static final String RESTOCK_QUEUE = "inventory.restock.queue";
    public static final String RESTOCK_ROUTING_KEY = "inventory.restock";

    // Exchanges
    @Bean
    public TopicExchange orderExchange() {
        return ExchangeBuilder.topicExchange(ORDER_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange inventoryExchange() {
        return ExchangeBuilder.topicExchange(INVENTORY_EXCHANGE)
                .durable(true)
                .build();
    }

    // Queues - Make all durable for consistency
    @Bean
    public Queue orderPlacedQueue() {
        return QueueBuilder.durable(ORDER_PLACED_QUEUE).build();
    }

    @Bean
    public Queue inventoryReservedQueue() {
        return QueueBuilder.durable(INVENTORY_RESERVED_QUEUE).build();
    }

    @Bean
    public Queue inventoryFailedQueue() {
        return QueueBuilder.durable(INVENTORY_FAILED_QUEUE).build();
    }

    // Bindings
    @Bean
    public Binding orderPlacedBinding() {
        return BindingBuilder.bind(orderPlacedQueue())
                .to(orderExchange())
                .with(ORDER_PLACED_ROUTING_KEY);
    }

    @Bean
    public Binding inventoryReservedBinding() {
        return BindingBuilder.bind(inventoryReservedQueue())
                .to(inventoryExchange())
                .with(INVENTORY_RESERVED_ROUTING_KEY);
    }

    @Bean
    public Binding inventoryFailedBinding() {
        return BindingBuilder.bind(inventoryFailedQueue())
                .to(inventoryExchange())
                .with(INVENTORY_FAILED_ROUTING_KEY);
    }

    // Message converter
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Optional: RabbitTemplate configuration
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        // Set default exchange if you want
        // template.setExchange(ORDER_EXCHANGE);
        return template;
    }

    @Bean
    public Queue restockQueue() {
        return new Queue(RESTOCK_QUEUE, true); // durable = true
    }

    @Bean
    public Binding restockBinding() {
        return BindingBuilder
                .bind(restockQueue())
                .to(inventoryExchange())
                .with(RESTOCK_ROUTING_KEY);
    }
}
