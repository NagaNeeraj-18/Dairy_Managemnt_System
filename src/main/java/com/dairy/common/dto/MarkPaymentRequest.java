package com.dairy.common.dto;

import com.dairy.common.enums.PaymentMode;
import jakarta.validation.constraints.NotNull;

public record MarkPaymentRequest(@NotNull PaymentMode paymentMode) {
}
