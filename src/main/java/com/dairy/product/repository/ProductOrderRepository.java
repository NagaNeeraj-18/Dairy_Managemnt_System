package com.dairy.product.repository;

import com.dairy.product.enums.ProductOrderStatus;

import com.dairy.product.entity.ProductOrder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface ProductOrderRepository extends JpaRepository<ProductOrder, Long> {
    List<ProductOrder> findByPincodeAndStatusOrderByIdAsc(String pincode, ProductOrderStatus status);

    Page<ProductOrder> findByCustomerId(Long customerId, Pageable pageable);

    long countByStatus(ProductOrderStatus status);

    @Query("select coalesce(sum(o.totalAmount), 0) from ProductOrder o where o.status = com.dairy.product.enums.ProductOrderStatus.DELIVERED")
    BigDecimal totalDeliveredRevenue();

    @Query(
        "select o.customer.id, count(o.id) from ProductOrder o " +
        "where o.status = :status group by o.customer.id"
    )
    List<Object[]> countDeliveredOrdersPerCustomer(@org.springframework.data.repository.query.Param("status") ProductOrderStatus status);
}
