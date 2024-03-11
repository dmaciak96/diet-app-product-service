package com.dietapp.productservice.service;

import com.dietapp.productservice.model.CreateProductMessage;
import com.dietapp.productservice.model.DeleteProductMessage;
import com.dietapp.productservice.model.ProductDto;
import com.dietapp.productservice.model.ProductType;
import com.dietapp.productservice.model.UpdateProductMessage;
import com.dietapp.productservice.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
public class ProductKafkaMessageHandlerTest {
    private static final String KAFKA_CONTAINER_NAME = "confluentinc/cp-kafka:7.4.0";
    private static final String POTATO = "Potato";
    private static final String KCAL_AFTER_BOILED = "KCAL_AFTER_BOILED";
    private static final String KCAL_AFTER_BOILED_VALUE = "66.0";
    private static final double POTATO_KCAL = 73.0;
    private static final ProductType POTATO_TYPE = ProductType.FRUITS_AND_VEGETABLES;

    @Value("${kafka.topic-name}")
    private String topicName;

    @Container
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse(KAFKA_CONTAINER_NAME));

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @AfterEach
    void afterEach() {
        productRepository.deleteAll();
    }

    @Test
    void shouldCreateNewProduct() {
        var createProductMessage = CreateProductMessage.builder()
                .name(POTATO)
                .kcal(POTATO_KCAL)
                .type(POTATO_TYPE)
                .properties(Map.of(KCAL_AFTER_BOILED, KCAL_AFTER_BOILED_VALUE))
                .build();

        await()
                .pollInterval(Duration.ofSeconds(1))
                .atMost(3, SECONDS)
                .untilAsserted(() -> {
                    kafkaTemplate.send(topicName, createProductMessage);

                    var result = productService.getAll(PageRequest.of(0, 1)).stream().findFirst();
                    assertTrue(result.isPresent());
                    var product = result.get();
                    assertEquals(POTATO, product.name());
                    assertEquals(POTATO_KCAL, product.kcal());
                    assertEquals(POTATO_TYPE, product.type());
                    assertTrue(product.properties().containsKey(KCAL_AFTER_BOILED));
                    assertEquals(KCAL_AFTER_BOILED_VALUE, product.properties().get(KCAL_AFTER_BOILED));
                });
    }

    @Test
    void shouldUpdateProduct() {
        var created = productService.create(ProductDto.builder()
                .name(POTATO)
                .kcal(POTATO_KCAL)
                .type(POTATO_TYPE)
                .properties(Map.of(KCAL_AFTER_BOILED, KCAL_AFTER_BOILED_VALUE))
                .build());

        var updateProductMessage = UpdateProductMessage.builder()
                .id(created.id())
                .name(POTATO + "_updated")
                .kcal(POTATO_KCAL + 1)
                .type(POTATO_TYPE)
                .properties(Map.of(KCAL_AFTER_BOILED, KCAL_AFTER_BOILED_VALUE))
                .build();

        await()
                .pollInterval(Duration.ofSeconds(1))
                .atMost(3, SECONDS)
                .untilAsserted(() -> {
                    kafkaTemplate.send(topicName, updateProductMessage);

                    var product = productService.getById(created.id());
                    assertEquals(POTATO + "_updated", product.name());
                    assertEquals(POTATO_KCAL + 1, product.kcal());
                    assertEquals(POTATO_TYPE, product.type());
                    assertTrue(product.properties().containsKey(KCAL_AFTER_BOILED));
                    assertEquals(KCAL_AFTER_BOILED_VALUE, product.properties().get(KCAL_AFTER_BOILED));
                });
    }

    @Test
    void shouldDeleteProduct() {
        var created = productService.create(ProductDto.builder()
                .name(POTATO)
                .kcal(POTATO_KCAL)
                .type(POTATO_TYPE)
                .properties(Map.of(KCAL_AFTER_BOILED, KCAL_AFTER_BOILED_VALUE))
                .build());

        var deleteProductMessage = new DeleteProductMessage(created.id());

        await()
                .pollInterval(Duration.ofSeconds(1))
                .atMost(3, SECONDS)
                .untilAsserted(() -> {
                    kafkaTemplate.send(topicName, deleteProductMessage);
                    assertFalse(productRepository.existsById(created.id()));
                });
    }
}
