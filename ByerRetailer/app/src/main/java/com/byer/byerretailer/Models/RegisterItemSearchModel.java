package com.byer.byerretailer.Models;

public class RegisterItemSearchModel {

    String brand;
    String name;
    String subcategory;
    String image;

    public RegisterItemSearchModel(){}


    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public RegisterItemSearchModel(String brand, String name, String subcategory, String image) {
        this.brand = brand;
        this.name = name;
        this.subcategory = subcategory;
        this.image = image;
    }



}
