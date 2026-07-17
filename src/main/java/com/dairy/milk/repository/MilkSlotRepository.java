package com.dairy.milk.repository;

import com.dairy.milk.entity.MilkSlot;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MilkSlotRepository extends JpaRepository<MilkSlot, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select slot from MilkSlot slot where slot.id = :slotId")
    Optional<MilkSlot> findByIdForUpdate(@Param("slotId") Long slotId);

    @Query("select coalesce(sum(slot.lostMilkMl), 0) from MilkSlot slot")
    int sumTotalLostMilk();

    @Query("select coalesce(sum(slot.totalMilkMl), 0) from MilkSlot slot")
    int sumTotalCapacityMilk();
}
