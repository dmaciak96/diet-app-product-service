package com.dietapp.productservice.service;

import com.dietapp.productservice.mapper.ProductMapper;
import com.dietapp.productservice.model.CreateProductMessage;
import com.dietapp.productservice.model.DeleteProductMessage;
import com.dietapp.productservice.model.NotificationCode;
import com.dietapp.productservice.model.NotificationMessage;
import com.dietapp.productservice.model.UpdateProductMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(id = "ProductService", topics = "product.service", groupId = "product.service")
public class ProductKafkaMessageHandler {
    private static final String NOTIFICATION_PRODUCT_PROPERTY_KEY = "product";

    private final ProductService productService;
    private final ProductMapper productMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.notification-topic-name}")
    private String notificationTopic;

    @KafkaHandler
    public void createProduct(CreateProductMessage message) {
        log.debug("Incoming Kafka Message: {}", message);
        try {
            var createdProduct = productService.create(productMapper.toDto(message));
            kafkaTemplate.send(notificationTopic, NotificationMessage.builder()
                    .message("New product was created (%s)".formatted(createdProduct.name()))
                    .code(NotificationCode.PRODUCT_CREATED)
                    .properties(Map.of(NOTIFICATION_PRODUCT_PROPERTY_KEY, createdProduct))
                    .build());
        } catch (Exception e) {
            log.error("Product creation error", e);
            kafkaTemplate.send(notificationTopic, createErrorNotification(e,
                    NotificationCode.PRODUCT_CREATED_ERROR));
        }
    }

    @KafkaHandler
    public void updateProduct(UpdateProductMessage message) {
        log.debug("Incoming Kafka Message: {}", message);
        try {
            var updatedProduct = productService.update(message.id(), productMapper.toDto(message));
            kafkaTemplate.send(notificationTopic, NotificationMessage.builder()
                    .message("Product was updated (%s)".formatted(updatedProduct.name()))
                    .code(NotificationCode.PRODUCT_UPDATED)
                    .properties(Map.of(NOTIFICATION_PRODUCT_PROPERTY_KEY, updatedProduct))
                    .build());
        } catch (Exception e) {
            log.error("Product update error", e);
            kafkaTemplate.send(notificationTopic, createErrorNotification(e,
                    NotificationCode.PRODUCT_UPDATED_ERROR));
        }
    }

    @KafkaHandler
    public void deleteProduct(DeleteProductMessage message) {
        log.debug("Incoming Kafka Message: {}", message);
        try {
            var removedProductName = productService.delete(message.id());
            kafkaTemplate.send(notificationTopic, NotificationMessage.builder()
                    .message("Product was removed (%s)".formatted(removedProductName))
                    .code(NotificationCode.PRODUCT_REMOVED)
                    .build());
        } catch (Exception e) {
            log.error("Product delete error", e);
            kafkaTemplate.send(notificationTopic, createErrorNotification(e,
                    NotificationCode.PRODUCT_REMOVED_ERROR));
        }
    }

    private NotificationMessage createErrorNotification(Exception e, NotificationCode code) {
        return NotificationMessage.builder()
                .code(code)
                .message(e.getMessage())
                .build();
    }
}
