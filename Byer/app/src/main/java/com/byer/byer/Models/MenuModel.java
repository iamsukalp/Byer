package com.byer.byer.Models;

public class MenuModel {
    String name;
    String category;
    String icon;
    boolean available;


    public MenuModel(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public MenuModel(String name, String category, String icon, boolean available) {
        this.name = name;
        this.category = category;
        this.icon = icon;
        this.available = available;
    }
}