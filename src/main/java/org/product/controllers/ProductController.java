package org.product.controllers;

import lombok.AllArgsConstructor;
import org.product.dto.ProductDto;
import org.product.services.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RequestMapping("/products")
@RestController
public class ProductController {
    private ProductService productService;

    @GetMapping("/fill")
    public void fillDatabase() {
        productService.fillDatabase();
    }

//    @GetMapping("/{productId}")
//    public Product getProduct(@PathVariable int productId) {
//        return productService.getProduct(productId);
//    }

    @GetMapping("/serve")
    public List<ProductDto> serve() {
        return productService.serve();
    }
}
