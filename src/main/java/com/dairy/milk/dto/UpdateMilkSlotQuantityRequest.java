package com.dairy.milk.dto;

import jakarta.validation.constraints.Min;

public record UpdateMilkSlotQuantityRequest(@Min(500) int totalMilkMl) {
}
