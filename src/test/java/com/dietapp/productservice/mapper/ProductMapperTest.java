package com.dietapp.productservice.mapper;

import com.dietapp.productservice.model.CustomProperty;
import com.dietapp.productservice.model.Product;
import com.dietapp.productservice.model.ProductDto;
import com.dietapp.productservice.model.ProductType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductMapperTest {

    private static final String POTATO = "Potato";
    private static final String KCAL_AFTER_BOILED = "KCAL_AFTER_BOILED";
    private static final String KCAL_AFTER_BOILED_VALUE = "66.0";
    private static final double POTATO_KCAL = 73.0;
    private static final UUID POTATO_UUID = UUID.randomUUID();
    private static final UUID PROPERTY_UUID = UUID.randomUUID();

    private ProductMapper productMapper;

    @BeforeEach
    void beforeAll() {
        this.productMapper = Mappers.getMapper(ProductMapper.class);
    }

    @Test
    void shouldDtoToEntity() {
        var entity = productMapper.toEntity(createProductDto());
        assertEquals(POTATO_UUID, entity.getId());
        assertEquals(POTATO, entity.getName());
        assertEquals(POTATO_KCAL, entity.getKcal());
        assertEquals(ProductType.FRUITS_AND_VEGETABLES, entity.getType());

        var properties = entity.getProperties();
        assertNotNull(properties);
        assertEquals(1, properties.size());

        var property = properties.stream().findFirst().get();
        assertEquals(entity, property.getProduct());
        assertEquals(KCAL_AFTER_BOILED, property.getName());
        assertEquals(KCAL_AFTER_BOILED_VALUE, property.getValue());
        assertNull(property.getId());
    }

    @Test
    void shouldMapEntityToDto() {
        var dto = productMapper.toDto(createProduct());
        assertEquals(POTATO_UUID, dto.id());
        assertEquals(POTATO, dto.name());
        assertEquals(POTATO_KCAL, dto.kcal());
        assertEquals(ProductType.FRUITS_AND_VEGETABLES, dto.type());

        var properties = dto.properties();
        assertNotNull(properties);
        assertTrue(properties.containsKey(KCAL_AFTER_BOILED));
        assertEquals(KCAL_AFTER_BOILED_VALUE, properties.get(KCAL_AFTER_BOILED));
    }

    private Product createProduct() {
        var product = Product.builder()
                .id(POTATO_UUID)
                .type(ProductType.FRUITS_AND_VEGETABLES)
                .name(POTATO)
                .kcal(POTATO_KCAL)
                .build();

        var property = CustomProperty.builder()
                .id(PROPERTY_UUID)
                .name(KCAL_AFTER_BOILED)
                .value(KCAL_AFTER_BOILED_VALUE)
                .product(product)
                .build();
        product.addProperty(property);
        return product;
    }

    private ProductDto createProductDto() {
        return ProductDto.builder()
                .id(POTATO_UUID)
                .name(POTATO)
                .kcal(POTATO_KCAL)
                .type(ProductType.FRUITS_AND_VEGETABLES)
                .properties(Map.of(KCAL_AFTER_BOILED, KCAL_AFTER_BOILED_VALUE))
                .build();
    }
}