package com.pulse.fineflux.service;

import com.pulse.fineflux.entity.Product;
import com.pulse.fineflux.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product createProduct(Product product) {
        try {
            Product savedProduct = productRepository.save(product);
            log.info("Product created successfully: {}", savedProduct.getProductName());
            return savedProduct;
        } catch (Exception e) {
            log.error("Error creating product: {}", e.getMessage());
            throw e;
        }
    }


    public List<Product> getAllProducts() {
        try {
            List<Product> products = productRepository.findAll();
            log.info("Fetched {} products", products.size());
            return products;
        } catch (Exception e) {
            log.error("Error fetching products: {}", e.getMessage());
            throw e;
        }
    }
}
