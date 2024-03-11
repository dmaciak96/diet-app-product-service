package com.dietapp.productservice.service;

import com.dietapp.productservice.exception.ProductNotFoundException;
import com.dietapp.productservice.mapper.ProductMapper;
import com.dietapp.productservice.model.Product;
import com.dietapp.productservice.model.ProductDto;
import com.dietapp.productservice.model.ProductType;
import com.dietapp.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceImplTest {

    private static final UUID PRODUCT_ID_ONE = UUID.randomUUID();
    private static final UUID PRODUCT_ID_TWO = UUID.randomUUID();
    private static final UUID PRODUCT_ID_THREE = UUID.randomUUID();
    private static final UUID PRODUCT_ID_NOT_EXISTS = UUID.randomUUID();
    private static final String POTATO = "Potato";

    private ProductService productService;
    private ProductRepository productRepository;

    @BeforeEach
    void beforeAll() {
        var productMapper = Mappers.getMapper(ProductMapper.class);
        this.productRepository = mock(ProductRepository.class);
        this.productService = new ProductServiceImpl(productRepository, productMapper);

        when(this.productRepository.findAll(any(Pageable.class))).thenAnswer(i -> createPageFromList(i.getArgument(0)));
        when(this.productRepository.findById(any())).thenAnswer(i -> createProductList().stream()
                .filter(product -> product.getId().equals(i.getArgument(0)))
                .findFirst());
        when(this.productRepository.existsById(PRODUCT_ID_ONE)).thenReturn(true);
        when(this.productRepository.existsById(PRODUCT_ID_THREE)).thenReturn(false);
        when(this.productRepository.saveAndFlush(any())).thenAnswer(i -> {
            var product = (Product) i.getArgument(0);
            return Product.builder()
                    .id(UUID.randomUUID())
                    .name(product.getName())
                    .type(product.getType())
                    .kcal(product.getKcal())
                    .properties(Set.of())
                    .build();
        });

    }

    @Test
    void getAllShouldReturnPageOfProducts() {
        var result = productService.getAll(PageRequest.of(0, 3))
                .get()
                .map(ProductDto::id)
                .collect(Collectors.toSet());
        assertTrue(result.contains(PRODUCT_ID_ONE));
        assertTrue(result.contains(PRODUCT_ID_TWO));
        assertTrue(result.contains(PRODUCT_ID_THREE));
    }

    @Test
    void getByIdShouldReturnProperProduct() {
        var result = productService.getById(PRODUCT_ID_ONE);
        assertEquals(PRODUCT_ID_ONE, result.id());
    }

    @Test
    void getByIdShouldThrowsNotFoundException() {
        assertThrows(ProductNotFoundException.class, () -> productService.getById(UUID.randomUUID()));
    }

    @Test
    void deleteShouldRemoveElement() {
        var deletedProductName = productService.delete(PRODUCT_ID_ONE);
        verify(productRepository).delete(any(Product.class));
        assertEquals(POTATO, deletedProductName);
    }

    @Test
    void deleteShouldThrowNotFoundException() {
        assertThrows(ProductNotFoundException.class, () -> productService.delete(PRODUCT_ID_NOT_EXISTS));
    }

    @Test
    void shouldCreateProductWithVersionCreationTimestampAndLastUpdate() {
        productService.create(ProductDto.builder()
                .name(POTATO)
                .kcal(73.0)
                .type(ProductType.FRUITS_AND_VEGETABLES)
                .properties(Map.of())
                .build());
        verify(productRepository).saveAndFlush(any());
    }

    @Test
    void updateShouldThrowsProductNotFoundException() {
        assertThrows(ProductNotFoundException.class, () ->
                productService.update(UUID.randomUUID(), ProductDto.builder().build()));
    }


    private List<Product> createProductList() {
        return List.of(Product.builder()
                        .id(PRODUCT_ID_ONE)
                        .properties(new HashSet<>())
                        .name(POTATO)
                        .build(),
                Product.builder()
                        .id(PRODUCT_ID_TWO)
                        .properties(new HashSet<>())
                        .name(POTATO)
                        .build(),
                Product.builder()
                        .id(PRODUCT_ID_THREE)
                        .properties(new HashSet<>())
                        .name(POTATO)
                        .build());
    }

    private Page<Product> createPageFromList(Pageable pageable) {
        var products = createProductList();

        var start = (int) pageable.getOffset();
        var end = Math.min((start + pageable.getPageSize()), products.size());
        var pageContent = products.subList(start, end);

        return new PageImpl<>(pageContent, pageable, products.size());
    }
}