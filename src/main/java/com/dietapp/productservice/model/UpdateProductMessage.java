package com.dietapp.productservice.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;

import java.util.Map;
import java.util.UUID;

@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UpdateProductMessage(UUID id,
                                   String name,
                                   double kcal,
                                   ProductType type,
                                   Map<String, String> properties) {
}
