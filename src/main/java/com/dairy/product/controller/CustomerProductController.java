package com.dairy.product.controller;

import com.dairy.common.dto.MarkPaymentRequest;
import com.dairy.security.service.AuthenticatedUserService;
import com.dairy.product.service.ProductService;

import com.dairy.product.dto.CreateProductOrderRequest;
import com.dairy.product.dto.ProductOrderResponse;
import com.dairy.product.dto.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer/products")
public class CustomerProductController {

    private final ProductService productService;
    private final AuthenticatedUserService authenticatedUserService;

    public CustomerProductController(ProductService productService, AuthenticatedUserService authenticatedUserService) {
        this.productService = productService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @GetMapping
    public Page<ProductResponse> getProducts(Pageable pageable) {
        return productService.getProducts(pageable).map(ProductResponse::from);
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductOrderResponse placeOrder(@Valid @RequestBody CreateProductOrderRequest request) {
        authenticatedUserService.requireCurrentUser(request.customerId());
        return ProductOrderResponse.from(productService.placeOrder(request));
    }

    @GetMapping("/orders/users/{customerId}")
    public Page<ProductOrderResponse> getCustomerOrders(@PathVariable Long customerId, Pageable pageable) {
        authenticatedUserService.requireCurrentUser(customerId);
        return productService.getCustomerOrders(customerId, pageable)
                .map(ProductOrderResponse::from);
    }

    @PostMapping("/orders/{orderId}/payment")
    public ProductOrderResponse markPaid(@PathVariable Long orderId, @Valid @RequestBody MarkPaymentRequest request) {
        productService.requireOrderCustomer(orderId, authenticatedUserService.currentUserId());
        return ProductOrderResponse.from(productService.markPaid(orderId, request.paymentMode()));
    }
}
