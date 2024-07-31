package com.byer.byerretailer.Models;

public class ItemModel {
    String name;
    String price;
    String quantity;
    String count;
    String unit;

    public ItemModel(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public ItemModel(String name, String price, String quantity, String count, String unit) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.count = count;
        this.unit = unit;
    }
}
