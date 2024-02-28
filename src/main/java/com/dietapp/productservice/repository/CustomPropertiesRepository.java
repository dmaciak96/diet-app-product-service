package com.dietapp.productservice.repository;

import com.dietapp.productservice.model.CustomProperty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface CustomPropertiesRepository extends JpaRepository<CustomProperty, UUID> {
    List<CustomProperty> findAllByProductId(UUID productId);
}
