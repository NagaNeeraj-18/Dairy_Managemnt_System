package com.dairy.product.entity;

import com.dairy.product.enums.ProductStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    @Test
    void stockCanBeReducedAndAdded() {
        Product product = new Product("Paneer", "packet", BigDecimal.valueOf(80), 5);

        product.reduceStock(2);
        product.addStock(4);

        assertThat(product.getStockQuantity()).isEqualTo(7);
    }

    @Test
    void reducingMoreThanAvailableStockFails() {
        Product product = new Product("Curd", "cup", BigDecimal.valueOf(30), 1);

        assertThatThrownBy(() -> product.reduceStock(2))
                .isInstanceOf(com.dairy.common.exception.InsufficientStockException.class)
                .hasMessageContaining("stock");
    }

    @Test
    void productCanBeDeactivatedAndReactivated() {
        Product product = new Product("Ghee", "jar", BigDecimal.valueOf(250), 3);

        product.deactivate();
        assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);

        product.activate();
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    void priceMustBePositive() {
        Product product = new Product("Butter", "pack", BigDecimal.valueOf(50), 2);

        assertThatThrownBy(() -> product.updatePrice(BigDecimal.ZERO))
                .isInstanceOf(com.dairy.common.exception.InvalidOperationException.class)
                .hasMessageContaining("Price");
    }
}
