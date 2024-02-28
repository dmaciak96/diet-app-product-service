package com.dietapp.productservice.repository;

import com.dietapp.productservice.model.CustomProperty;
import com.dietapp.productservice.model.Product;
import com.dietapp.productservice.model.ProductType;
import jakarta.validation.ConstraintViolationException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomPropertiesRepository customPropertiesRepository;

    @Test
    void shouldSuccessfullyCreateProductWithProperties() {
        var createdProduct = productRepository.save(createProduct());
        assertNotNull(createdProduct.getId());
        assertNotNull(createdProduct.getProperties());

        var properties = customPropertiesRepository.findAllByProductId(createdProduct.getId());
        assertEquals(1, properties.size());
        assertNotNull(properties.get(0).getId());
    }

    @Test
    void shouldThrowExceptionWhenProductNameIsNull() {
        var product = createProduct();
        product.setName(null);
        assertThrows(ConstraintViolationException.class, () -> productRepository.saveAndFlush(product));
    }

    @Test
    void shouldThrowExceptionWhenProductNameIsEmpty() {
        var product = createProduct();
        product.setName("");
        assertThrows(ConstraintViolationException.class, () -> productRepository.saveAndFlush(product));
    }

    @Test
    void shouldThrowExceptionWhenKcalIsLessThanZero() {
        var product = createProduct();
        product.setKcal(-1.0);
        assertThrows(ConstraintViolationException.class, () -> productRepository.saveAndFlush(product));
    }

    @Test
    void shouldThrowExceptionWhenPropertyNameIsNull() {
        var product = createProduct();
        product.getProperties().stream().findFirst().get().setName(null);
        assertThrows(ConstraintViolationException.class, () -> productRepository.saveAndFlush(product));
    }

    @Test
    void shouldThrowExceptionWhenPropertyNameIsEmpty() {
        var product = createProduct();
        product.getProperties().stream().findFirst().get().setName(StringUtils.EMPTY);
        assertThrows(ConstraintViolationException.class, () -> productRepository.saveAndFlush(product));
    }

    @Test
    void shouldThrowExceptionWhenPropertyValueIsNull() {
        var product = createProduct();
        product.getProperties().stream().findFirst().get().setValue(null);
        assertThrows(ConstraintViolationException.class, () -> productRepository.saveAndFlush(product));
    }

    @Test
    void shouldThrowExceptionWhenPropertyValueIsEmpty() {
        var product = createProduct();
        product.getProperties().stream().findFirst().get().setValue(StringUtils.EMPTY);
        assertThrows(ConstraintViolationException.class, () -> productRepository.saveAndFlush(product));
    }

    @Test
    void shouldRemoveProductWithAllProperties() {
        var savedProduct = productRepository.save(createProduct());
        assertEquals(1, customPropertiesRepository.findAllByProductId(savedProduct.getId()).size());
        productRepository.deleteById(savedProduct.getId());
        assertTrue(customPropertiesRepository.findAllByProductId(savedProduct.getId()).isEmpty());
    }

    @Test
    void shouldSetVersionAndTimestamps() {
        var savedProduct = productRepository.saveAndFlush(createProduct());
        assertEquals(0, savedProduct.getVersion());
        assertNotNull(savedProduct.getCreatedDate());
        assertNotNull(savedProduct.getLastUpdatedDate());
    }

    private Product createProduct() {
        var product = Product.builder()
                .type(ProductType.FRUITS_AND_VEGETABLES)
                .name("Potato")
                .kcal(73.0)
                .build();

        var property = CustomProperty.builder()
                .name("KCAL_AFTER_BOILED")
                .value("66.0")
                .product(product)
                .build();
        product.addProperty(property);
        return product;
    }
}