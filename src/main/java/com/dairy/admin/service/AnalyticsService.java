package com.dairy.admin.service;

import com.dairy.admin.repository.OfflineSaleRepository;
import com.dairy.admin.repository.ExpenseRepository;
import com.dairy.admin.dto.AnalyticsResponse;
import com.dairy.feedback.repository.FeedbackRepository;
import com.dairy.milk.enums.BookingStatus;
import com.dairy.milk.repository.MilkBookingRepository;
import com.dairy.milk.repository.MilkSlotRepository;
import com.dairy.product.repository.ProductOrderRepository;
import com.dairy.product.enums.ProductOrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);
    private final MilkBookingRepository milkBookingRepository;
    private final ProductOrderRepository productOrderRepository;
    private final OfflineSaleRepository offlineSaleRepository;
    private final ExpenseRepository expenseRepository;
    private final FeedbackRepository feedbackRepository;
    private final MilkSlotRepository milkSlotRepository;

    public AnalyticsService(
            MilkBookingRepository milkBookingRepository,
            ProductOrderRepository productOrderRepository,
            OfflineSaleRepository offlineSaleRepository,
            ExpenseRepository expenseRepository,
            FeedbackRepository feedbackRepository,
            MilkSlotRepository milkSlotRepository
    ) {
        this.milkBookingRepository = milkBookingRepository;
        this.productOrderRepository = productOrderRepository;
        this.offlineSaleRepository = offlineSaleRepository;
        this.expenseRepository = expenseRepository;
        this.feedbackRepository = feedbackRepository;
        this.milkSlotRepository = milkSlotRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsResponse getSummary() {
        log.info("Calculating business analytics and summaries");
        BigDecimal productRevenue = productOrderRepository.totalDeliveredRevenue();
        BigDecimal offlineRevenue = offlineSaleRepository.totalOfflineRevenue();
        BigDecimal totalRevenue = productRevenue.add(offlineRevenue);
        BigDecimal totalExpenses = expenseRepository.totalExpenses();

        log.debug("Calculated revenue: Product={}, Offline={}, Total={}. Expenses={}", 
                productRevenue, offlineRevenue, totalRevenue, totalExpenses);

        // Milk loss metrics
        int totalLostMilk = milkSlotRepository.sumTotalLostMilk();
        int totalCapacity = milkSlotRepository.sumTotalCapacityMilk();
        double milkLossRate = totalCapacity == 0 ? 0.0 : (totalLostMilk * 100.0) / totalCapacity;

        // Customer retention metrics (defined as: active users with >= 2 delivered bookings/orders)
        Map<Long, Long> customerDeliveredCount = new HashMap<>();
        
        List<Object[]> bookingsCount = milkBookingRepository.countDeliveredBookingsPerCustomer(BookingStatus.DELIVERED);
        for (Object[] row : bookingsCount) {
            Long customerId = (Long) row[0];
            Long count = (Long) row[1];
            customerDeliveredCount.put(customerId, count);
        }

        List<Object[]> ordersCount = productOrderRepository.countDeliveredOrdersPerCustomer(ProductOrderStatus.DELIVERED);
        for (Object[] row : ordersCount) {
            Long customerId = (Long) row[0];
            Long count = (Long) row[1];
            customerDeliveredCount.put(customerId, customerDeliveredCount.getOrDefault(customerId, 0L) + count);
        }

        long totalCustomers = customerDeliveredCount.size();
        long repeatCustomers = customerDeliveredCount.values().stream().filter(cnt -> cnt >= 2).count();
        double customerRetentionRate = totalCustomers == 0 ? 0.0 : (repeatCustomers * 100.0) / totalCustomers;

        AnalyticsResponse response = new AnalyticsResponse(
                milkBookingRepository.countByStatus(BookingStatus.DELIVERED),
                productOrderRepository.countByStatus(ProductOrderStatus.DELIVERED),
                productRevenue,
                offlineRevenue,
                totalRevenue,
                totalExpenses,
                totalRevenue.subtract(totalExpenses),
                feedbackRepository.averageProductRating(),
                feedbackRepository.averageDeliveryBoyRating(),
                totalLostMilk,
                milkLossRate,
                customerRetentionRate
        );
        log.info("Analytics summary calculated successfully");
        return response;
    }
}
