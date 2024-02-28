package com.dietapp.productservice.model;

import lombok.Builder;

import java.util.Map;
import java.util.UUID;

@Builder
public record ProductDto(UUID id,
                         String name,
                         double kcal,
                         ProductType type,
                         Map<String, String> properties) {
}
