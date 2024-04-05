package com.playtech.assignment.pojo;

public class Balance {
    private final String userId;
    private double balance;

    public Balance(String userId, double balance) {
        this.userId = userId;
        this.balance = balance;
    }

    public String getUserId() {
        return userId;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
