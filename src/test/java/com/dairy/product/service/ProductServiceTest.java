package com.dairy.product.service;

import com.dairy.common.enums.PaymentMode;
import com.dairy.common.exception.InvalidOperationException;
import com.dairy.common.exception.InvalidRoleException;
import com.dairy.common.exception.ResourceNotFoundException;
import com.dairy.common.exception.InsufficientStockException;
import com.dairy.product.dto.CreateProductOrderRequest;
import com.dairy.product.dto.CreateProductRequest;
import com.dairy.product.entity.Product;
import com.dairy.product.entity.ProductOrder;
import com.dairy.product.enums.ProductOrderStatus;
import com.dairy.product.enums.ProductStatus;
import com.dairy.product.repository.ProductOrderRepository;
import com.dairy.product.repository.ProductRepository;
import com.dairy.user.entity.Address;
import com.dairy.user.entity.AppUser;
import com.dairy.user.enums.UserRole;
import com.dairy.user.service.AddressService;
import com.dairy.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductOrderRepository productOrderRepository;

    @Mock
    private UserService userService;

    @Mock
    private AddressService addressService;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private AppUser customer;
    private Address address;

    @BeforeEach
    void setUp() {
        product = new Product("Milk Packet", "Packet", BigDecimal.valueOf(30), 100);
        customer = mock(AppUser.class);
        when(customer.getId()).thenReturn(1L);
        when(customer.getRole()).thenReturn(UserRole.CUSTOMER);
        address = mock(Address.class);
        when(address.toSingleLine()).thenReturn("123 Street, City");
        when(address.getPincode()).thenReturn("560001");
    }

    @Test
    void createProduct_Success() {
        CreateProductRequest request = new CreateProductRequest("Milk Packet", "Packet", BigDecimal.valueOf(30), 100);
        when(productRepository.save(any())).thenReturn(product);

        Product result = productService.createProduct(request);

        assertNotNull(result);
        assertEquals("Milk Packet", result.getName());
    }

    @Test
    void getProducts_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findAll(pageable)).thenReturn(page);

        Page<Product> result = productService.getProducts(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getProduct_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product result = productService.getProduct(1L);

        assertNotNull(result);
        assertEquals("Milk Packet", result.getName());
    }

    @Test
    void getProduct_NotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProduct(1L));
    }

    @Test
    void setStock_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product result = productService.setStock(1L, 200);

        assertEquals(200, result.getStockQuantity());
    }

    @Test
    void addStock_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product result = productService.addStock(1L, 50);

        assertEquals(150, result.getStockQuantity());
    }

    @Test
    void updatePrice_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product result = productService.updatePrice(1L, BigDecimal.valueOf(35));

        assertEquals(BigDecimal.valueOf(35), result.getPrice());
    }

    @Test
    void activate_Deactivate_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deactivate(1L);
        assertEquals(ProductStatus.INACTIVE, product.getStatus());

        productService.activate(1L);
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
    }

    @Test
    void placeOrder_Success() {
        CreateProductOrderRequest request = new CreateProductOrderRequest(1L, 1L, 2, null, null, null);
        when(userService.getUser(1L)).thenReturn(customer);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(addressService.getDefaultAddress(1L)).thenReturn(address);

        ProductOrder mockOrder = new ProductOrder(customer, product, 2, "123 Street, City", "560001");
        when(productOrderRepository.save(any())).thenReturn(mockOrder);

        ProductOrder result = productService.placeOrder(request);

        assertNotNull(result);
        assertEquals(98, product.getStockQuantity());
        verify(productOrderRepository, times(1)).save(any());
    }

    @Test
    void placeOrder_InvalidRole() {
        CreateProductOrderRequest request = new CreateProductOrderRequest(1L, 1L, 2, null, null, null);
        AppUser adminUser = mock(AppUser.class);
        when(adminUser.getRole()).thenReturn(UserRole.ADMIN);
        when(userService.getUser(1L)).thenReturn(adminUser);

        assertThrows(InvalidRoleException.class, () -> productService.placeOrder(request));
    }

    @Test
    void placeOrder_ProductInactive() {
        CreateProductOrderRequest request = new CreateProductOrderRequest(1L, 1L, 2, null, null, null);
        product.deactivate();
        when(userService.getUser(1L)).thenReturn(customer);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(InvalidOperationException.class, () -> productService.placeOrder(request));
    }

    @Test
    void placeOrder_InsufficientStock() {
        CreateProductOrderRequest request = new CreateProductOrderRequest(1L, 1L, 200, null, null, null);
        when(userService.getUser(1L)).thenReturn(customer);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(addressService.getDefaultAddress(1L)).thenReturn(address);

        assertThrows(InsufficientStockException.class, () -> productService.placeOrder(request));
    }

    @Test
    void markPaid_Success() {
        ProductOrder mockOrder = new ProductOrder(customer, product, 2, "123 Street, City", "560001");
        when(productOrderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));

        ProductOrder result = productService.markPaid(1L, PaymentMode.ONLINE_AFTER_DELIVERY);

        assertNotNull(result);
        assertEquals(com.dairy.common.enums.PaymentStatus.PAID, result.getPaymentStatus());
    }
}
