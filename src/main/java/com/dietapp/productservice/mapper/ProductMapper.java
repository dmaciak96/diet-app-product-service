package com.dietapp.productservice.mapper;

import com.dietapp.productservice.model.CustomProperty;
import com.dietapp.productservice.model.Product;
import com.dietapp.productservice.model.ProductDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.stream.Collectors;

@Mapper(builder = @Builder(disableBuilder = true))
public interface ProductMapper {

    @Mapping(target = "properties", ignore = true)
    ProductDto toDto(Product product);

    @AfterMapping
    default ProductDto mapDtoProperties(Product product, @MappingTarget ProductDto productDto) {
        var propertiesMap = product.getProperties().stream()
                .collect(Collectors.toMap(CustomProperty::getName, CustomProperty::getValue));

        return ProductDto.builder()
                .id(productDto.id())
                .name(productDto.name())
                .kcal(productDto.kcal())
                .type(productDto.type())
                .properties(propertiesMap)
                .build();
    }

    @Mapping(target = "properties", ignore = true)
    Product toEntity(ProductDto dto);

    @AfterMapping
    default Product mapEntityProperties(@MappingTarget Product product, ProductDto productDto) {
        var propertiesSet = productDto.properties().entrySet().stream()
                .map(entry -> CustomProperty.builder()
                        .name(entry.getKey())
                        .value(entry.getValue())
                        .product(product)
                        .build())
                .collect(Collectors.toSet());

        product.setProperties(propertiesSet);
        return product;
    }
}
