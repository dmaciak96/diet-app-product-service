package com.dietapp.productservice.service;

import com.dietapp.productservice.model.ProductDto;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface ProductService {
    Page<ProductDto> getAll(int pageNumber, int pageSize);

    Optional<ProductDto> getById(UUID id);

    ProductDto create(ProductDto productDto);

    ProductDto update(UUID id, ProductDto productDto);

    void delete(UUID id);
}
