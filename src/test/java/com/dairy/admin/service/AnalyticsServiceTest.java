package com.dairy.admin.service;

import com.dairy.admin.dto.AnalyticsResponse;
import com.dairy.admin.repository.ExpenseRepository;
import com.dairy.admin.repository.OfflineSaleRepository;
import com.dairy.feedback.repository.FeedbackRepository;
import com.dairy.milk.enums.BookingStatus;
import com.dairy.milk.repository.MilkBookingRepository;
import com.dairy.milk.repository.MilkSlotRepository;
import com.dairy.product.enums.ProductOrderStatus;
import com.dairy.product.repository.ProductOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private MilkBookingRepository milkBookingRepository;

    @Mock
    private ProductOrderRepository productOrderRepository;

    @Mock
    private OfflineSaleRepository offlineSaleRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private MilkSlotRepository milkSlotRepository;

    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService(
                milkBookingRepository,
                productOrderRepository,
                offlineSaleRepository,
                expenseRepository,
                feedbackRepository,
                milkSlotRepository
        );
    }

    @Test
    void getSummary_Success() {
        // Mock revenue and expenses
        when(productOrderRepository.totalDeliveredRevenue()).thenReturn(new BigDecimal("100.00"));
        when(offlineSaleRepository.totalOfflineRevenue()).thenReturn(new BigDecimal("50.00"));
        when(expenseRepository.totalExpenses()).thenReturn(new BigDecimal("30.00"));

        // Mock ratings and counts
        when(milkBookingRepository.countByStatus(BookingStatus.DELIVERED)).thenReturn(10L);
        when(productOrderRepository.countByStatus(ProductOrderStatus.DELIVERED)).thenReturn(5L);
        when(feedbackRepository.averageProductRating()).thenReturn(4.5);
        when(feedbackRepository.averageDeliveryBoyRating()).thenReturn(4.8);

        // Mock milk loss
        when(milkSlotRepository.sumTotalLostMilk()).thenReturn(500);
        when(milkSlotRepository.sumTotalCapacityMilk()).thenReturn(10000);

        // Mock customer retention data:
        // Customer 1: 2 bookings (repeat)
        // Customer 2: 1 booking, 1 product order (repeat)
        // Customer 3: 1 product order
        List<Object[]> bookingsCount = new ArrayList<>();
        bookingsCount.add(new Object[]{1L, 2L});
        bookingsCount.add(new Object[]{2L, 1L});
        when(milkBookingRepository.countDeliveredBookingsPerCustomer(BookingStatus.DELIVERED)).thenReturn(bookingsCount);

        List<Object[]> ordersCount = new ArrayList<>();
        ordersCount.add(new Object[]{2L, 1L});
        ordersCount.add(new Object[]{3L, 1L});
        when(productOrderRepository.countDeliveredOrdersPerCustomer(ProductOrderStatus.DELIVERED)).thenReturn(ordersCount);

        AnalyticsResponse response = analyticsService.getSummary();

        assertNotNull(response);
        assertEquals(10L, response.deliveredMilkOrders());
        assertEquals(5L, response.deliveredProductOrders());
        assertEquals(new BigDecimal("100.00"), response.productRevenue());
        assertEquals(new BigDecimal("50.00"), response.offlineRevenue());
        assertEquals(new BigDecimal("150.00"), response.totalRevenue());
        assertEquals(new BigDecimal("30.00"), response.totalExpenses());
        assertEquals(new BigDecimal("120.00"), response.netAmount());
        assertEquals(4.5, response.averageProductRating());
        assertEquals(4.8, response.averageDeliveryBoyRating());
        
        // Assert new metrics
        assertEquals(500, response.totalLostMilkMl());
        assertEquals(5.0, response.milkLossRate(), 0.001);
        // Repeat customer count: 2 (Customer 1, Customer 2) out of 3 total. Retention rate: (2/3) * 100 = 66.666...
        assertEquals(66.66666666666667, response.customerRetentionRate(), 0.001);
    }
}
