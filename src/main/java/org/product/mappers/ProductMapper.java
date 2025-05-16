package org.product.mappers;

import org.mapstruct.Mapper;
import org.product.dto.ProductDto;
import org.product.entities.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDto toDto(Product product);
}
