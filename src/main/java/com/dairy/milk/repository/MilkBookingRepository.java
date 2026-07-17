package com.dairy.milk.repository;

import com.dairy.milk.enums.BookingStatus;
import com.dairy.milk.entity.MilkBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MilkBookingRepository extends JpaRepository<MilkBooking, Long> {
    Page<MilkBooking> findByMilkSlotId(Long milkSlotId, Pageable pageable);

    List<MilkBooking> findByPincodeAndStatusOrderByIdAsc(String pincode, BookingStatus status);

    Page<MilkBooking> findByCustomerId(Long customerId, Pageable pageable);

    long countByStatus(BookingStatus status);

    @org.springframework.data.jpa.repository.Query(
        "select b.customer.id, count(b.id) from MilkBooking b " +
        "where b.status = :status group by b.customer.id"
    )
    List<Object[]> countDeliveredBookingsPerCustomer(@org.springframework.data.repository.query.Param("status") BookingStatus status);
}
