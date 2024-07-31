package com.byer.byer.Models;

public class CartModel {
    String name;
    String count;
    String price;
    String quantity;
    String retailerId;
    String itemId;
    String category;
    String distance;
    String unit;


    public CartModel(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getRetailerId() {
        return retailerId;
    }

    public void setRetailerId(String retailerId) {
        this.retailerId = retailerId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public CartModel(String name, String count, String price, String quantity, String retailerId, String itemId, String category, String distance, String unit) {
        this.name = name;
        this.count = count;
        this.price = price;
        this.quantity = quantity;
        this.retailerId = retailerId;
        this.itemId = itemId;
        this.category = category;
        this.distance = distance;
        this.unit = unit;
    }
}
