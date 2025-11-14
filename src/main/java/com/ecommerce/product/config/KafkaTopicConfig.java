package com.ecommerce.product.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic ordersTopic() {
        return TopicBuilder.name("orders")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentsTopic() {
        return TopicBuilder.name("payments")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic stocksTopic() {
        return TopicBuilder.name("stocks")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
