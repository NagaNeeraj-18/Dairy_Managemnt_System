package com.dairy.milk.repository;

import com.dairy.milk.enums.WaitingListStatus;

import com.dairy.milk.entity.WaitingListEntry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WaitingListRepository extends JpaRepository<WaitingListEntry, Long> {
    List<WaitingListEntry> findByMilkSlotIdAndStatusOrderByCreatedAtAsc(Long milkSlotId, WaitingListStatus status);
}
