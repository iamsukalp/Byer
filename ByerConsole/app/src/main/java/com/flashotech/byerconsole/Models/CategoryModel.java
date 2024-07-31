package com.flashotech.byerconsole.Models;

public class CategoryModel {
    String name;
    String category;
    String icon;
    boolean available;

    public CategoryModel(){}

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

    public CategoryModel(String name, String category, String icon, boolean available) {
        this.name = name;
        this.category = category;
        this.icon = icon;
        this.available = available;
    }
}
