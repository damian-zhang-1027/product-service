package com.ecommerce.product.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BaseEventDto(
        String eventType) {
}
