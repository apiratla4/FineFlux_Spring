package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.ProductCreateDTO;
import com.pulse.fineflux.domain.ProductResponseDTO;
import com.pulse.fineflux.domain.ProductUpdateDTO;

import java.util.List;

public interface ProductService {

    List<ProductResponseDTO> getAllProducts(String orgId);

    ProductResponseDTO getProduct(String orgId, String productId);

    ProductResponseDTO createProduct(ProductCreateDTO dto);

    ProductResponseDTO updateProduct(String orgId, String productId, ProductUpdateDTO dto);

    void deleteProduct(String orgId, String productId);
}
