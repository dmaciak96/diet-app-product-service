package com.dietapp.productservice.service;

import com.dietapp.productservice.exception.ProductNotFoundException;
import com.dietapp.productservice.mapper.ProductMapper;
import com.dietapp.productservice.model.CustomProperty;
import com.dietapp.productservice.model.Product;
import com.dietapp.productservice.model.ProductDto;
import com.dietapp.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public Page<ProductDto> getAll(Pageable pageable) {
        log.info("Get all products (Page number: {}, Page size: {})", pageable.getPageNumber(), pageable.getPageSize());
        return productRepository.findAll(pageable)
                .map(productMapper::toDto);
    }

    @Override
    public ProductDto getById(UUID id) {
        log.info("Get product by id {}", id);
        var product = productRepository.findById(id);
        return productMapper.toDto(product.orElseThrow(() ->
                new ProductNotFoundException("Product not found by id %s".formatted(id))));
    }

    @Override
    public ProductDto create(ProductDto productDto) {
        log.info("Saving new product (name: {}, kcal: {}, type: {})",
                productDto.name(), productDto.kcal(), productDto.type());
        var product = productMapper.toEntity(productDto);
        product.setVersion(0);

        var savedProduct = productRepository.saveAndFlush(product);
        log.info("Product {} was saved (id: {})", savedProduct.getName(), savedProduct.getId());

        return productMapper.toDto(savedProduct);
    }

    @Override
    public void delete(UUID id) {
        log.info("Removing product (id: {})", id);
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found by id %s".formatted(id));
        }
        productRepository.deleteById(id);
        log.info("Product {} was removed", id);
    }

    @Override
    public ProductDto update(UUID id, ProductDto productDto) {
        log.info("Updating product (name: {}, id: {})", productDto.name(), id);
        var productToUpdate = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found by id %s".formatted(id)));

        productToUpdate.setName(productDto.name());
        productToUpdate.setKcal(productDto.kcal());
        productToUpdate.setType(productDto.type());
        productToUpdate.setProperties(mapToCustomProperties(productDto.properties(), productToUpdate));

        var updatedProduct = productRepository.saveAndFlush(productToUpdate);
        log.info("Product {} was updated (id: {})", updatedProduct.getName(), updatedProduct.getId());

        return productMapper.toDto(updatedProduct);
    }

    private Set<CustomProperty> mapToCustomProperties(Map<String, String> properties, Product product) {
        return properties.entrySet().stream()
                .map(entry -> CustomProperty.builder()
                        .name(entry.getKey())
                        .value(entry.getValue())
                        .product(product)
                        .build())
                .collect(Collectors.toSet());
    }
}
