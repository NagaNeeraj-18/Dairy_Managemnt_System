package com.dairy.milk.enums;

import java.util.Arrays;

public enum MilkQuantity {
    ML_500(500),
    ML_1000(1000),
    ML_1500(1500),
    ML_2000(2000);

    private final int milliliters;

    MilkQuantity(int milliliters) {
        this.milliliters = milliliters;
    }

    public int getMilliliters() {
        return milliliters;
    }

    public static MilkQuantity fromMilliliters(int milliliters) {
        return Arrays.stream(values())
                .filter(quantity -> quantity.milliliters == milliliters)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Allowed quantities are 500, 1000, 1500 and 2000 ml"));
    }
}
