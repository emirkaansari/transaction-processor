package com.playtech.assignment.pojo;

import java.util.concurrent.atomic.AtomicLong;

public class User {
    private final AtomicLong balanceInCents;
    private String id;
    private String username;
    private String country;
    private Boolean frozen;
    private double depositMin;
    private double depositMax;
    private double withdrawMin;
    private double withdrawMax;

    public User(String id, String username, double balance, String country,
                Boolean frozen, double depositMin, double depositMax, double withdrawMin, double withdrawMax) {
        this.id = id;
        this.username = username;
        this.balanceInCents = new AtomicLong((long) (balance * 100));
        this.country = country;
        this.frozen = frozen;
        this.depositMin = depositMin;
        this.depositMax = depositMax;
        this.withdrawMin = withdrawMin;
        this.withdrawMax = withdrawMax;
    }

    public User(User other) {
        this.id = other.id;
        this.username = other.username;
        this.balanceInCents = other.balanceInCents;
        this.country = other.country;
        this.frozen = other.frozen;
        this.depositMin = other.depositMin;
        this.depositMax = other.depositMax;
        this.withdrawMin = other.withdrawMin;
        this.withdrawMax = other.withdrawMax;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getBalance() {
        return balanceInCents.get() / 100.0;
    }

    public void setBalance(double balance) {
        this.balanceInCents.set((long) (balance * 100));
    }

    public void addBalance(double amount) {
        this.balanceInCents.addAndGet((long) (amount * 100));
    }

    public void subtractBalance(double amount) {
        this.balanceInCents.addAndGet((long) (-amount * 100));
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Boolean getFrozen() {
        return frozen;
    }

    public void setFrozen(Boolean frozen) {
        this.frozen = frozen;
    }

    public double getDepositMin() {
        return depositMin;
    }

    public void setDepositMin(double depositMin) {
        this.depositMin = depositMin;
    }

    public double getDepositMax() {
        return depositMax;
    }

    public void setDepositMax(double depositMax) {
        this.depositMax = depositMax;
    }

    public double getWithdrawMin() {
        return withdrawMin;
    }

    public void setWithdrawMin(double withdrawMin) {
        this.withdrawMin = withdrawMin;
    }

    public double getWithdrawMax() {
        return withdrawMax;
    }

    public void setWithdrawMax(double withdrawMax) {
        this.withdrawMax = withdrawMax;
    }
}
