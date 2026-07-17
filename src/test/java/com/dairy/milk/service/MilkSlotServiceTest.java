package com.dairy.milk.service;

import com.dairy.common.exception.InvalidOperationException;
import com.dairy.common.exception.ResourceNotFoundException;
import com.dairy.milk.dto.CreateMilkSlotRequest;
import com.dairy.milk.entity.MilkSlot;
import com.dairy.milk.enums.DeliverySlot;
import com.dairy.milk.enums.WindowStatus;
import com.dairy.milk.repository.MilkSlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MilkSlotServiceTest {

    @Mock
    private MilkSlotRepository milkSlotRepository;

    @InjectMocks
    private MilkSlotService milkSlotService;

    private CreateMilkSlotRequest createMilkSlotRequest;
    private MilkSlot milkSlot;

    @BeforeEach
    void setUp() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDateTime opens = tomorrow.minusDays(1).atTime(17, 0);
        LocalDateTime closes = tomorrow.minusDays(1).atTime(22, 0);
        LocalDateTime cancels = tomorrow.minusDays(1).atTime(23, 0);
        createMilkSlotRequest = new CreateMilkSlotRequest(tomorrow, DeliverySlot.MORNING, 1000, opens, closes, cancels);
        milkSlot = new MilkSlot(tomorrow, DeliverySlot.MORNING, 1000, opens, closes, cancels);
    }

    @Test
    void createSlot_Success() {
        when(milkSlotRepository.save(any())).thenReturn(milkSlot);

        MilkSlot result = milkSlotService.createSlot(createMilkSlotRequest);

        assertNotNull(result);
        assertEquals(1000, result.getTotalMilkMl());
        verify(milkSlotRepository, times(1)).save(any());
    }

    @Test
    void createSlot_InvalidQuantity() {
        CreateMilkSlotRequest invalidRequest = new CreateMilkSlotRequest(
                LocalDate.now().plusDays(1), DeliverySlot.MORNING, 999, null, null, null
        );

        assertThrows(InvalidOperationException.class, () -> milkSlotService.createSlot(invalidRequest));
        verify(milkSlotRepository, never()).save(any());
    }

    @Test
    void getAllSlots_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<MilkSlot> page = new PageImpl<>(List.of(milkSlot));
        when(milkSlotRepository.findAll(pageable)).thenReturn(page);

        Page<MilkSlot> result = milkSlotService.getAllSlots(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void closeBooking_Success() {
        when(milkSlotRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(milkSlot));

        MilkSlot result = milkSlotService.closeBooking(1L);

        assertEquals(WindowStatus.CLOSED, result.getBookingStatus());
    }

    @Test
    void updateQuantity_Success() {
        when(milkSlotRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(milkSlot));

        MilkSlot result = milkSlotService.updateQuantity(1L, 1500);

        assertEquals(1500, result.getTotalMilkMl());
        assertEquals(1500, result.getAvailableMilkMl());
    }

    @Test
    void updateQuantity_InvalidMultiple() {
        assertThrows(InvalidOperationException.class, () -> milkSlotService.updateQuantity(1L, 1234));
    }

    @Test
    void getSlot_Success() {
        when(milkSlotRepository.findById(1L)).thenReturn(Optional.of(milkSlot));

        MilkSlot result = milkSlotService.getSlot(1L);

        assertNotNull(result);
        assertEquals(1000, result.getTotalMilkMl());
    }

    @Test
    void getSlot_NotFound() {
        when(milkSlotRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> milkSlotService.getSlot(1L));
    }

    @Test
    void recordMilkLoss_Success() {
        when(milkSlotRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(milkSlot));

        MilkSlot result = milkSlotService.recordMilkLoss(1L, 200);

        assertEquals(200, result.getLostMilkMl());
    }

    @Test
    void recordMilkLoss_NegativeQuantity() {
        when(milkSlotRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(milkSlot));

        assertThrows(InvalidOperationException.class, () -> milkSlotService.recordMilkLoss(1L, -100));
    }
}
