package com.bikundo.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.core.Queue;


@Configuration
@EnableRabbit
public class RabbitMQConfig {

    public static final String ORDER_EXCHANGE = "order.exchange";

    public static final String ORDER_PLACED_QUEUE = "order.placed.queue";
    public static final String ORDER_RESULT_QUEUE = "order.result.queue";
    public static final String ORDER_CANCEL_QUEUE = "order.cancel.queue";

    public static final String ORDER_PLACED_ROUTING_KEY = "order.placed";
    public static final String ORDER_RESULT_ROUTING_KEY = "order.result";
    public static final String ORDER_CANCEL_ROUTING_KEY = "order.cancel";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Queue placedQueue() {
        return new Queue(ORDER_PLACED_QUEUE);
    }

    @Bean
    public Queue resultQueue() {
        return new Queue(ORDER_RESULT_QUEUE);
    }

    @Bean
    public Queue cancelQueue() {
        return new Queue(ORDER_CANCEL_QUEUE);
    }

    @Bean
    public Binding placedBinding() {
        return BindingBuilder.bind(placedQueue())
                .to(orderExchange())
                .with(ORDER_PLACED_ROUTING_KEY);
    }

    @Bean
    public Binding resultBinding() {
        return BindingBuilder.bind(resultQueue())
                .to(orderExchange())
                .with(ORDER_RESULT_ROUTING_KEY);
    }

    @Bean
    public Binding cancelBinding() {
        return BindingBuilder.bind(cancelQueue())
                .to(orderExchange())
                .with(ORDER_CANCEL_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
