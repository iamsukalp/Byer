package com.byer.byer.Models;

public class RetailerModel {
    String name;
    String image;
    String category;
    String email;
    String phone;
    String address;
    String sublocality;

    public RetailerModel(){}

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSublocality() {
        return sublocality;
    }

    public void setSublocality(String sublocality) {
        this.sublocality = sublocality;
    }

    public RetailerModel(String name, String image, String category, String email, String phone, String address, String sublocality) {
        this.name = name;
        this.image = image;
        this.category = category;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.sublocality = sublocality;
    }
}
