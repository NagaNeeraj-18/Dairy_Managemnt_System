package com.dairy.product.service;

import com.dairy.common.enums.PaymentMode;
import com.dairy.user.entity.Address;
import com.dairy.user.service.AddressService;
import com.dairy.product.repository.ProductRepository;

import com.dairy.product.repository.ProductOrderRepository;

import com.dairy.product.enums.ProductStatus;

import com.dairy.product.enums.ProductOrderStatus;

import com.dairy.product.entity.ProductOrder;

import com.dairy.product.entity.Product;

import com.dairy.product.dto.CreateProductOrderRequest;
import com.dairy.product.dto.CreateProductRequest;
import com.dairy.user.entity.AppUser;
import com.dairy.user.enums.UserRole;
import com.dairy.user.service.UserService;
import com.dairy.common.exception.ResourceNotFoundException;
import com.dairy.common.exception.InvalidRoleException;
import com.dairy.common.exception.InvalidOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    private final ProductOrderRepository productOrderRepository;
    private final UserService userService;
    private final AddressService addressService;

    public ProductService(ProductRepository productRepository, ProductOrderRepository productOrderRepository, UserService userService, AddressService addressService) {
        this.productRepository = productRepository;
        this.productOrderRepository = productOrderRepository;
        this.userService = userService;
        this.addressService = addressService;
    }

    @Transactional
    public Product createProduct(CreateProductRequest request) {
        log.info("Creating new product: {} with price: {} and stock: {}", request.name(), request.price(), request.stockQuantity());
        Product savedProduct = productRepository.save(new Product(request.name(), request.unitLabel(), request.price(), request.stockQuantity()));
        log.info("Product created successfully with ID: {}", savedProduct.getId());
        return savedProduct;
    }

    @Transactional(readOnly = true)
    public Page<Product> getProducts(Pageable pageable) {
        log.debug("Fetching paginated products with pageable: {}", pageable);
        return productRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Product getProduct(Long productId) {
        log.debug("Fetching product ID: {}", productId);
        return productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product not found with ID: {}", productId);
                    return new ResourceNotFoundException("Product not found: " + productId);
                });
    }

    @Transactional
    public Product setStock(Long productId, int stockQuantity) {
        log.info("Setting stock for product ID: {} to quantity: {}", productId, stockQuantity);
        Product product = getProduct(productId);
        product.setStockQuantity(stockQuantity);
        log.info("Product ID: {} stock set to: {}", productId, product.getStockQuantity());
        return product;
    }

    @Transactional
    public Product addStock(Long productId, int quantityToAdd) {
        log.info("Adding stock to product ID: {}, quantity to add: {}", productId, quantityToAdd);
        Product product = getProduct(productId);
        product.addStock(quantityToAdd);
        log.info("Product ID: {} stock updated. New stock: {}", productId, product.getStockQuantity());
        return product;
    }

    @Transactional
    public Product updatePrice(Long productId, java.math.BigDecimal price) {
        log.info("Updating price for product ID: {} to: {}", productId, price);
        Product product = getProduct(productId);
        product.updatePrice(price);
        log.info("Product ID: {} price updated to: {}", productId, product.getPrice());
        return product;
    }

    @Transactional
    public Product activate(Long productId) {
        log.info("Activating product ID: {}", productId);
        Product product = getProduct(productId);
        product.activate();
        log.info("Product ID: {} status set to ACTIVE", productId);
        return product;
    }

    @Transactional
    public Product deactivate(Long productId) {
        log.info("Deactivating product ID: {}", productId);
        Product product = getProduct(productId);
        product.deactivate();
        log.info("Product ID: {} status set to INACTIVE", productId);
        return product;
    }

    @Transactional
    public ProductOrder placeOrder(CreateProductOrderRequest request) {
        log.info("Placing order for customer ID: {}, product ID: {}, quantity: {}", request.customerId(), request.productId(), request.quantity());
        AppUser customer = userService.getUser(request.customerId());
        if (customer.getRole() != UserRole.CUSTOMER) {
            log.warn("Order placement failed: User ID {} does not have CUSTOMER role", request.customerId());
            throw new InvalidRoleException("User must have role CUSTOMER");
        }
        Product product = getProduct(request.productId());
        if (product.getStatus() != ProductStatus.ACTIVE) {
            log.warn("Order placement failed: Product ID {} is not active", request.productId());
            throw new InvalidOperationException("Product is not active");
        }
        ResolvedAddress resolvedAddress = resolveAddress(customer.getId(), request.addressId(), request.deliveryAddress(), request.pincode());
        
        try {
            product.reduceStock(request.quantity());
        } catch (Exception e) {
            log.warn("Order placement failed: Insufficient stock for product ID: {}", product.getId());
            throw e;
        }

        ProductOrder order = productOrderRepository.save(new ProductOrder(
                customer,
                product,
                request.quantity(),
                resolvedAddress.deliveryAddress(),
                resolvedAddress.pincode()
        ));
        log.info("Product order placed successfully with ID: {} for customer ID: {}", order.getId(), customer.getId());
        return order;
    }

    @Transactional(readOnly = true)
    public ProductOrder getProductOrder(Long orderId) {
        log.debug("Fetching product order ID: {}", orderId);
        return productOrderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Product order not found with ID: {}", orderId);
                    return new ResourceNotFoundException("Product order not found: " + orderId);
                });
    }

    @Transactional(readOnly = true)
    public List<ProductOrder> getPlacedOrdersByPincode(String pincode) {
        log.debug("Fetching placed product orders for pincode: {}", pincode);
        return productOrderRepository.findByPincodeAndStatusOrderByIdAsc(pincode, ProductOrderStatus.PLACED);
    }

    @Transactional(readOnly = true)
    public Page<ProductOrder> getCustomerOrders(Long customerId, Pageable pageable) {
        log.debug("Fetching paginated customer orders for customer ID: {}, pageable: {}", customerId, pageable);
        return productOrderRepository.findByCustomerId(customerId, pageable);
    }

    @Transactional
    public ProductOrder markPaid(Long orderId, PaymentMode paymentMode) {
        log.info("Marking product order ID: {} as paid via: {}", orderId, paymentMode);
        ProductOrder order = getProductOrder(orderId);
        order.markPaid(paymentMode);
        log.info("Product order ID: {} payment status updated to PAID", orderId);
        return order;
    }

    @Transactional(readOnly = true)
    public void requireOrderCustomer(Long orderId, Long customerId) {
        log.debug("Validating customer ID: {} ownership of order ID: {}", customerId, orderId);
        ProductOrder order = getProductOrder(orderId);
        if (!order.getCustomer().getId().equals(customerId)) {
            log.warn("Ownership validation failed. Order ID: {} does not belong to customer ID: {}", orderId, customerId);
            throw new AccessDeniedException("You can access only your own product order");
        }
    }

    private ResolvedAddress resolveAddress(Long customerId, Long addressId, String deliveryAddress, String pincode) {
        if (addressId != null) {
            Address address = addressService.getAddressForUser(customerId, addressId);
            return new ResolvedAddress(address.toSingleLine(), address.getPincode());
        }
        if (deliveryAddress == null || deliveryAddress.isBlank() || pincode == null || pincode.isBlank()) {
            Address address = addressService.getDefaultAddress(customerId);
            return new ResolvedAddress(address.toSingleLine(), address.getPincode());
        }
        return new ResolvedAddress(deliveryAddress, pincode);
    }

    private record ResolvedAddress(String deliveryAddress, String pincode) {
    }
}
