package com.dairy.admin.dto;

import com.dairy.admin.entity.Expense;

import java.math.BigDecimal;

public record ExpenseResponse(Long id, String category, BigDecimal amount, String note) {
    public static ExpenseResponse from(Expense expense) {
        return new ExpenseResponse(expense.getId(), expense.getCategory(), expense.getAmount(), expense.getNote());
    }
}
