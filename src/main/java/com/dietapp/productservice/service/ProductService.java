package com.dietapp.productservice.service;

import com.dietapp.productservice.model.ProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductService {
    Page<ProductDto> getAll(Pageable pageable);

    ProductDto getById(UUID id);

    ProductDto create(ProductDto productDto);

    ProductDto update(UUID id, ProductDto productDto);

    String delete(UUID id);
}
