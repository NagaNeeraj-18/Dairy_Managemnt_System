package com.dairy.admin.controller;

import com.dairy.admin.service.AdminBusinessService;

import com.dairy.admin.dto.CreateExpenseRequest;
import com.dairy.admin.dto.CreateOfflineSaleRequest;
import com.dairy.admin.dto.ExpenseResponse;
import com.dairy.admin.dto.OfflineSaleResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/business")
public class AdminBusinessController {

    private final AdminBusinessService adminBusinessService;

    public AdminBusinessController(AdminBusinessService adminBusinessService) {
        this.adminBusinessService = adminBusinessService;
    }

    @PostMapping("/offline-sales")
    @ResponseStatus(HttpStatus.CREATED)
    public OfflineSaleResponse createOfflineSale(@Valid @RequestBody CreateOfflineSaleRequest request) {
        return OfflineSaleResponse.from(adminBusinessService.createOfflineSale(request));
    }

    @GetMapping("/offline-sales")
    public Page<OfflineSaleResponse> getOfflineSales(Pageable pageable) {
        return adminBusinessService.getOfflineSales(pageable).map(OfflineSaleResponse::from);
    }

    @PostMapping("/expenses")
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse createExpense(@Valid @RequestBody CreateExpenseRequest request) {
        return ExpenseResponse.from(adminBusinessService.createExpense(request));
    }

    @GetMapping("/expenses")
    public Page<ExpenseResponse> getExpenses(Pageable pageable) {
        return adminBusinessService.getExpenses(pageable).map(ExpenseResponse::from);
    }
}
