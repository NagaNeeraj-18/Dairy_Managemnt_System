package com.dairy.delivery.service;

import com.dairy.delivery.dto.AssignByPincodeRequest;
import com.dairy.delivery.entity.DeliveryAssignment;
import com.dairy.delivery.enums.DeliveryStatus;
import com.dairy.delivery.enums.OrderType;
import com.dairy.delivery.repository.DeliveryAssignmentRepository;
import com.dairy.common.exception.InvalidRoleException;
import com.dairy.common.exception.ResourceNotFoundException;
import com.dairy.common.exception.OtpVerificationException;
import com.dairy.common.exception.InvalidOperationException;
import com.dairy.milk.entity.MilkBooking;
import com.dairy.milk.service.MilkBookingService;
import com.dairy.notification.service.NotificationService;
import com.dairy.product.entity.ProductOrder;
import com.dairy.product.service.ProductService;
import com.dairy.user.entity.AppUser;
import com.dairy.user.enums.UserRole;
import com.dairy.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DeliveryServiceTest {

    @Mock
    private DeliveryAssignmentRepository deliveryAssignmentRepository;

    @Mock
    private MilkBookingService milkBookingService;

    @Mock
    private ProductService productService;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private DeliveryService deliveryService;

    private AppUser deliveryBoy;
    private AppUser customer;
    private MilkBooking milkBooking;
    private ProductOrder productOrder;
    private DeliveryAssignment deliveryAssignment;

    @BeforeEach
    void setUp() {
        deliveryBoy = mock(AppUser.class);
        when(deliveryBoy.getId()).thenReturn(1L);
        when(deliveryBoy.getRole()).thenReturn(UserRole.DELIVERY_BOY);

        customer = mock(AppUser.class);
        when(customer.getId()).thenReturn(2L);

        milkBooking = mock(MilkBooking.class);
        when(milkBooking.getId()).thenReturn(10L);
        when(milkBooking.getCustomer()).thenReturn(customer);
        when(milkBooking.getDeliveryAddress()).thenReturn("123 Street");
        when(milkBooking.getPincode()).thenReturn("560001");

        productOrder = mock(ProductOrder.class);
        when(productOrder.getId()).thenReturn(20L);
        when(productOrder.getCustomer()).thenReturn(customer);
        when(productOrder.getDeliveryAddress()).thenReturn("456 Avenue");
        when(productOrder.getPincode()).thenReturn("560001");

        deliveryAssignment = new DeliveryAssignment(deliveryBoy, OrderType.MILK, 10L, "123 Street", "560001", "123456");
    }

    @Test
    void assignMilkOrders_Success() {
        AssignByPincodeRequest request = new AssignByPincodeRequest(1L, "560001");
        when(userService.getUser(1L)).thenReturn(deliveryBoy);
        when(milkBookingService.getConfirmedBookingsByPincode("560001")).thenReturn(List.of(milkBooking));
        when(deliveryAssignmentRepository.existsByOrderTypeAndOrderId(OrderType.MILK, 10L)).thenReturn(false);
        when(deliveryAssignmentRepository.save(any())).thenReturn(deliveryAssignment);

        List<DeliveryAssignment> result = deliveryService.assignMilkOrders(request);

        assertEquals(1, result.size());
        verify(milkBooking, times(1)).markAssignedToDelivery();
        verify(notificationService, times(1)).create(any(), anyString(), anyString());
    }

    @Test
    void assignMilkOrders_InvalidRole() {
        AssignByPincodeRequest request = new AssignByPincodeRequest(1L, "560001");
        AppUser invalidUser = mock(AppUser.class);
        when(invalidUser.getRole()).thenReturn(UserRole.CUSTOMER);
        when(userService.getUser(1L)).thenReturn(invalidUser);

        assertThrows(InvalidRoleException.class, () -> deliveryService.assignMilkOrders(request));
    }

    @Test
    void assignProductOrders_Success() {
        AssignByPincodeRequest request = new AssignByPincodeRequest(1L, "560001");
        when(userService.getUser(1L)).thenReturn(deliveryBoy);
        when(productService.getPlacedOrdersByPincode("560001")).thenReturn(List.of(productOrder));
        when(deliveryAssignmentRepository.existsByOrderTypeAndOrderId(OrderType.PRODUCT, 20L)).thenReturn(false);
        
        DeliveryAssignment prodAssignment = new DeliveryAssignment(deliveryBoy, OrderType.PRODUCT, 20L, "456 Avenue", "560001", "654321");
        when(deliveryAssignmentRepository.save(any())).thenReturn(prodAssignment);

        List<DeliveryAssignment> result = deliveryService.assignProductOrders(request);

        assertEquals(1, result.size());
        verify(productOrder, times(1)).markAssignedToDelivery();
        verify(notificationService, times(1)).create(any(), anyString(), anyString());
    }

    @Test
    void track_Success() {
        when(deliveryAssignmentRepository.findByOrderTypeAndOrderId(OrderType.MILK, 10L))
                .thenReturn(Optional.of(deliveryAssignment));

        DeliveryAssignment result = deliveryService.track(OrderType.MILK, 10L);

        assertNotNull(result);
        assertEquals("123456", result.getOtp());
    }

    @Test
    void track_NotFound() {
        when(deliveryAssignmentRepository.findByOrderTypeAndOrderId(OrderType.MILK, 10L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> deliveryService.track(OrderType.MILK, 10L));
    }

    @Test
    void markOutForDelivery_Success() {
        when(deliveryAssignmentRepository.findById(1L)).thenReturn(Optional.of(deliveryAssignment));
        when(milkBookingService.getBooking(10L)).thenReturn(milkBooking);

        DeliveryAssignment result = deliveryService.markOutForDelivery(1L);

        assertEquals(DeliveryStatus.OUT_FOR_DELIVERY, result.getStatus());
        verify(milkBooking, times(1)).markOutForDelivery();
    }

    @Test
    void markDelivered_Success() {
        // Let's set it as OUT_FOR_DELIVERY first so it satisfies condition
        deliveryAssignment.markOutForDelivery();
        when(deliveryAssignmentRepository.findById(1L)).thenReturn(Optional.of(deliveryAssignment));
        when(milkBookingService.getBooking(10L)).thenReturn(milkBooking);

        DeliveryAssignment result = deliveryService.markDelivered(1L, "123456");

        assertEquals(DeliveryStatus.DELIVERED, result.getStatus());
        verify(milkBooking, times(1)).markDelivered();
    }

    @Test
    void markDelivered_OtpMismatch() {
        when(deliveryAssignmentRepository.findById(1L)).thenReturn(Optional.of(deliveryAssignment));

        assertThrows(OtpVerificationException.class, () -> deliveryService.markDelivered(1L, "000000"));
    }
}
