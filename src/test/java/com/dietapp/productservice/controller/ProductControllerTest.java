package com.dietapp.productservice.controller;

import com.dietapp.productservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int DEFAULT_PAGE_SIZE = 25;


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    void shouldReturnDefaultProductPage() {

    }

    @Test
    void shouldReturnSpecifiedProductPage() {

    }

    @Test
    void shouldReturnProductById() {

    }

    @Test
    void shouldReturn404NotFoundWhenProductNotFoundById() {

    }
}