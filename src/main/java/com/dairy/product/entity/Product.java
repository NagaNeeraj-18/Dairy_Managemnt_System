package com.dairy.product.entity;

import com.dairy.product.enums.ProductStatus;
import com.dairy.common.exception.InsufficientStockException;
import com.dairy.common.exception.InvalidOperationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String unitLabel;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private int stockQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Product() {
    }

    public Product(String name, String unitLabel, BigDecimal price, int stockQuantity) {
        this.name = name;
        this.unitLabel = unitLabel;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public void reduceStock(int quantity) {
        if (stockQuantity < quantity) {
            throw new InsufficientStockException("Not enough product stock available");
        }
        stockQuantity -= quantity;
    }

    public void setStockQuantity(int stockQuantity) {
        if (stockQuantity < 0) {
            throw new InvalidOperationException("Stock quantity cannot be negative");
        }
        this.stockQuantity = stockQuantity;
    }

    public void addStock(int quantityToAdd) {
        if (quantityToAdd <= 0) {
            throw new InvalidOperationException("Quantity to add must be positive");
        }
        stockQuantity += quantityToAdd;
    }

    public void updatePrice(BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new InvalidOperationException("Price must be positive");
        }
        this.price = price;
    }

    public void activate() {
        status = ProductStatus.ACTIVE;
    }

    public void deactivate() {
        status = ProductStatus.INACTIVE;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUnitLabel() {
        return unitLabel;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public ProductStatus getStatus() {
        return status;
    }
}
