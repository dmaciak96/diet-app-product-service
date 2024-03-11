package com.dietapp.productservice.controller;

import com.dietapp.productservice.exception.ProductNotFoundException;
import com.dietapp.productservice.mapper.ProductMapper;
import com.dietapp.productservice.model.ProductDto;
import com.dietapp.productservice.model.ProductType;
import com.dietapp.productservice.service.ProductService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {
    private static final String POTATO = "Potato";
    private static final String KCAL_AFTER_BOILED = "KCAL_AFTER_BOILED";
    private static final String KCAL_AFTER_BOILED_VALUE = "66.0";
    private static final double POTATO_KCAL = 73.0;
    private static final UUID POTATO_UUID = UUID.randomUUID();
    private static final Instant CREATED_DATE = Instant.now();
    private static final Instant UPDATE_DATE = Instant.now();


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private ProductMapper productMapper;

    @BeforeEach
    void beforeEach() {
        when(productService.getAll(any(Pageable.class))).thenAnswer(i -> {
            var pageable = (Pageable) i.getArgument(0);
            return new PageImpl<>(createProducts(pageable.getPageSize()),
                    pageable, 300);
        });
        when(productMapper.toHttpResponse(any(ProductDto.class)))
                .thenAnswer(i -> Mappers.getMapper(ProductMapper.class).toHttpResponse(i.getArgument(0)));
        when(productService.getById(eq(POTATO_UUID)))
                .thenReturn(createProducts(1).get(0));
    }

    @Test
    void shouldReturnDefaultProductPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.hasSize(25)));
    }

    @Test
    void shouldReturnSpecifiedProductPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/products")
                        .queryParam("pageNumber", "2")
                        .queryParam("pageSize", "50")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", Matchers.hasSize(50)));
    }

    @Test
    void shouldReturnProductById() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/products/" + POTATO_UUID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(POTATO_UUID.toString()))
                .andExpect(jsonPath("$.name").value(POTATO))
                .andExpect(jsonPath("$.kcal").value(POTATO_KCAL))
                .andExpect(jsonPath("$.type").value(ProductType.FRUITS_AND_VEGETABLES.name()))
                .andExpect(jsonPath("$.properties.KCAL_AFTER_BOILED").value(KCAL_AFTER_BOILED_VALUE));
    }

    @Test
    void shouldReturn404NotFoundWhenProductNotFoundById() throws Exception {
        when(productService.getById(any(UUID.class)))
                .thenThrow(new ProductNotFoundException("Product not found by id"));
        mockMvc.perform(MockMvcRequestBuilders.get("/products/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    private List<ProductDto> createProducts(int size) {
        return IntStream.range(0, size)
                .mapToObj(e -> ProductDto.builder()
                        .id(POTATO_UUID)
                        .name(POTATO)
                        .kcal(POTATO_KCAL)
                        .type(ProductType.FRUITS_AND_VEGETABLES)
                        .createdDate(CREATED_DATE)
                        .lastUpdatedDate(UPDATE_DATE)
                        .properties(Map.of(KCAL_AFTER_BOILED, KCAL_AFTER_BOILED_VALUE))
                        .build())
                .toList();
    }
}