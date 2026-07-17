package com.dairy.milk.dto;

import jakarta.validation.constraints.Min;

public record RecordMilkLossRequest(
        @Min(value = 1, message = "Lost milk quantity must be at least 1 ml")
        int lostMilkMl
) {
}
