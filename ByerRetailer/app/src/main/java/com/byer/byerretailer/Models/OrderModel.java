package com.byer.byerretailer.Models;

public class OrderModel {

    String userId;
    String retailerId;
    String amount;
    long timestamp;
    String paymentMode;
    String deliveryMode;
    String userName;
    String shopName;
    String category;

    public OrderModel(){}

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public OrderModel(String userId, String retailerId, String amount, long timestamp, String paymentMode, String deliveryMode, String userName, String shopName, String category) {
        this.userId = userId;
        this.retailerId = retailerId;
        this.amount = amount;
        this.timestamp = timestamp;
        this.paymentMode = paymentMode;
        this.deliveryMode = deliveryMode;
        this.userName = userName;
        this.shopName = shopName;
        this.category = category;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRetailerId() {
        return retailerId;
    }

    public void setRetailerId(String retailerId) {
        this.retailerId = retailerId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getDeliveryMode() {
        return deliveryMode;
    }

    public void setDeliveryMode(String deliveryMode) {
        this.deliveryMode = deliveryMode;
    }


}
