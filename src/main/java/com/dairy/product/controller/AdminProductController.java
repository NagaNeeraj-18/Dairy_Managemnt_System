package com.dairy.product.controller;

import com.dairy.product.service.ProductService;
import com.dairy.product.dto.AddProductStockRequest;
import com.dairy.product.dto.CreateProductRequest;
import com.dairy.product.dto.ProductResponse;
import com.dairy.product.dto.UpdateProductPriceRequest;
import com.dairy.product.dto.UpdateProductStockRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody CreateProductRequest request) {
        return ProductResponse.from(productService.createProduct(request));
    }

    @GetMapping
    public Page<ProductResponse> getProducts(Pageable pageable) {
        return productService.getProducts(pageable).map(ProductResponse::from);
    }

    @PutMapping("/{productId}/stock")
    public ProductResponse setStock(@PathVariable Long productId, @Valid @RequestBody UpdateProductStockRequest request) {
        return ProductResponse.from(productService.setStock(productId, request.stockQuantity()));
    }

    @PatchMapping("/{productId}/stock")
    public ProductResponse addStock(@PathVariable Long productId, @Valid @RequestBody AddProductStockRequest request) {
        return ProductResponse.from(productService.addStock(productId, request.quantityToAdd()));
    }

    @PutMapping("/{productId}/price")
    public ProductResponse updatePrice(@PathVariable Long productId, @Valid @RequestBody UpdateProductPriceRequest request) {
        return ProductResponse.from(productService.updatePrice(productId, request.price()));
    }

    @PatchMapping("/{productId}/activate")
    public ProductResponse activate(@PathVariable Long productId) {
        return ProductResponse.from(productService.activate(productId));
    }

    @PatchMapping("/{productId}/deactivate")
    public ProductResponse deactivate(@PathVariable Long productId) {
        return ProductResponse.from(productService.deactivate(productId));
    }
}
