package com.byer.byerretailer.Models;

public class ProductListModel {

    boolean available;
    String name;
    String image;
    String unit;
    String mrp;
    String sp;
    String quantity;
    String description;
    String subcategory;
    String soldCount;
    String totalAvailableQuantity;

    public ProductListModel(){}

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getMrp() {
        return mrp;
    }

    public void setMrp(String mrp) {
        this.mrp = mrp;
    }

    public String getSp() {
        return sp;
    }

    public void setSp(String sp) {
        this.sp = sp;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String getTotalAvailableQuantity() {
        return totalAvailableQuantity;
    }

    public void setTotalAvailableQuantity(String totalAvailableQuantity) {
        this.totalAvailableQuantity = totalAvailableQuantity;
    }

    public String getSoldCount() {
        return soldCount;
    }

    public void setSoldCount(String soldCount) {
        this.soldCount = soldCount;
    }

    public ProductListModel(boolean available, String name, String image, String unit, String mrp, String sp, String quantity, String description, String subcategory, String soldCount, String totalAvailableQuantity) {
        this.available = available;
        this.name = name;
        this.image = image;
        this.unit = unit;
        this.mrp = mrp;
        this.sp = sp;
        this.quantity = quantity;
        this.description = description;
        this.subcategory = subcategory;
        this.soldCount = soldCount;
        this.totalAvailableQuantity = totalAvailableQuantity;
    }
}
