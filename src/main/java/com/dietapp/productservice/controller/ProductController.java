package com.dietapp.productservice.controller;

import com.dietapp.productservice.mapper.ProductMapper;
import com.dietapp.productservice.model.ProductHttpResponse;
import com.dietapp.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ProductController.PRODUCTS_ENDPOINT)
public class ProductController {
    public static final String PRODUCTS_ENDPOINT = "/products";

    private final ProductService productService;
    private final ProductMapper productMapper;

    @GetMapping
    public ResponseEntity<Page<ProductHttpResponse>> getAllProducts(@RequestParam(required = false, defaultValue = "0") int pageNumber,
                                                                    @RequestParam(required = false, defaultValue = "25") int pageSize) {
        var products = productService.getAll(PageRequest.of(pageNumber, pageSize));
        return ResponseEntity
                .ok(products.map(productMapper::toHttpResponse));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductHttpResponse> getProductById(@PathVariable UUID productId) {
        var productDto = productService.getById(productId);
        return ResponseEntity.ok(productMapper.toHttpResponse(productDto));
    }
}
