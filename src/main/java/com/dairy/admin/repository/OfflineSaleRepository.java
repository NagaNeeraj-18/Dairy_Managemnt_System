package com.dairy.admin.repository;

import com.dairy.admin.entity.OfflineSale;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface OfflineSaleRepository extends JpaRepository<OfflineSale, Long> {
    @Query("select coalesce(sum(s.amount), 0) from OfflineSale s")
    BigDecimal totalOfflineRevenue();
}
