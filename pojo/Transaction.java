package com.playtech.assignment.pojo;

public class Transaction {
    public static final String METHOD_CARD = "CARD";
    public static final String METHOD_TRANSFER = "TRANSFER";
    public static final String TYPE_DEPOSIT = "DEPOSIT";
    public static final String TYPE_WITHDRAW = "WITHDRAW";
    private String id;
    private String userId;
    private String type;
    private double amount;
    private String method;
    private String accountNumber;

    public Transaction(String id, String userId, String type, double amount, String method, String accountNumber) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.method = method;
        this.accountNumber = accountNumber;
    }

    public Transaction(Transaction other) {
        this.id = other.id;
        this.userId = other.userId;
        this.type = other.type;
        this.amount = other.amount;
        this.method = other.method;
        this.accountNumber = other.accountNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}
