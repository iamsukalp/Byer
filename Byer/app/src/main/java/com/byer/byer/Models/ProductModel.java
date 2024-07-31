package com.byer.byer.Models;

public class ProductModel {

    String image;
    String name;
    String unit;
    String mrp;
    String quantity;
    String description;
    String sp;
    String subcategory;
    boolean available;


    public ProductModel(){}

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getSp() {
        return sp;
    }

    public void setSp(String sp) {
        this.sp = sp;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public ProductModel(String image, String name, String unit, String mrp, String quantity, String description, String sp, String subcategory, boolean available) {
        this.image = image;
        this.name = name;
        this.unit = unit;
        this.mrp = mrp;
        this.quantity = quantity;
        this.description = description;
        this.sp = sp;
        this.subcategory = subcategory;
        this.available = available;
    }
}
