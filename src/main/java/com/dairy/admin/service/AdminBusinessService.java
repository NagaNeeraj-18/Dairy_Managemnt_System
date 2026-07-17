package com.dairy.admin.service;

import com.dairy.admin.repository.OfflineSaleRepository;
import com.dairy.admin.repository.ExpenseRepository;
import com.dairy.admin.entity.OfflineSale;
import com.dairy.admin.entity.Expense;
import com.dairy.admin.dto.CreateExpenseRequest;
import com.dairy.admin.dto.CreateOfflineSaleRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminBusinessService {

    private static final Logger log = LoggerFactory.getLogger(AdminBusinessService.class);
    private final OfflineSaleRepository offlineSaleRepository;
    private final ExpenseRepository expenseRepository;

    public AdminBusinessService(OfflineSaleRepository offlineSaleRepository, ExpenseRepository expenseRepository) {
        this.offlineSaleRepository = offlineSaleRepository;
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public OfflineSale createOfflineSale(CreateOfflineSaleRequest request) {
        log.info("Recording new offline sale: {} - Quantity: {} {} - Amount: {}", 
                request.itemName(), request.quantity(), request.unitLabel(), request.amount());
        OfflineSale sale = offlineSaleRepository.save(new OfflineSale(
                request.itemName(),
                request.quantity(),
                request.unitLabel(),
                request.amount(),
                request.customerName()
        ));
        log.info("Offline sale recorded successfully with ID: {}", sale.getId());
        return sale;
    }

    @Transactional
    public Expense createExpense(CreateExpenseRequest request) {
        log.info("Recording new expense: Category: {} - Amount: {}", request.category(), request.amount());
        Expense expense = expenseRepository.save(new Expense(request.category(), request.amount(), request.note()));
        log.info("Expense recorded successfully with ID: {}", expense.getId());
        return expense;
    }

    @Transactional(readOnly = true)
    public Page<OfflineSale> getOfflineSales(Pageable pageable) {
        log.debug("Fetching paginated offline sales: {}", pageable);
        return offlineSaleRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Expense> getExpenses(Pageable pageable) {
        log.debug("Fetching paginated expenses: {}", pageable);
        return expenseRepository.findAll(pageable);
    }
}
