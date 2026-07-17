package com.dairy.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private AppUser user;

    @NotBlank
    @Column(nullable = false)
    private String line1;

    private String line2;

    @NotBlank
    @Column(nullable = false)
    private String city;

    @NotBlank
    @Column(nullable = false)
    private String pincode;

    @Column(nullable = false)
    private boolean defaultAddress;

    protected Address() {
    }

    public Address(AppUser user, String line1, String line2, String city, String pincode, boolean defaultAddress) {
        this.user = user;
        this.line1 = line1;
        this.line2 = line2;
        this.city = city;
        this.pincode = pincode;
        this.defaultAddress = defaultAddress;
    }

    public void markDefault() {
        defaultAddress = true;
    }

    public void clearDefault() {
        defaultAddress = false;
    }

    public String toSingleLine() {
        if (line2 == null || line2.isBlank()) {
            return line1 + ", " + city;
        }
        return line1 + ", " + line2 + ", " + city;
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public String getLine1() {
        return line1;
    }

    public String getLine2() {
        return line2;
    }

    public String getCity() {
        return city;
    }

    public String getPincode() {
        return pincode;
    }

    public boolean isDefaultAddress() {
        return defaultAddress;
    }
}
