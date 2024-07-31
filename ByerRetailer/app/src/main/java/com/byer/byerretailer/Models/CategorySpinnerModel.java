package com.byer.byerretailer.Models;

import com.byer.byerretailer.R;

import java.util.ArrayList;

public class CategorySpinnerModel {
    private static final ArrayList<CategorySpinnerModel> CategorySpinnerModelArrayList = new ArrayList<>();

    private String id;
    private String name;



    public CategorySpinnerModel(String id, String name)
    {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static void initCategorySpinnerModels()
    {
        CategorySpinnerModel CategorySpinnerModel1 = new CategorySpinnerModel("0", "Select a category");
        CategorySpinnerModelArrayList.add(CategorySpinnerModel1);

        CategorySpinnerModel CategorySpinnerModel2 = new CategorySpinnerModel("1", "Grocery");
        CategorySpinnerModelArrayList.add(CategorySpinnerModel2);

        CategorySpinnerModel CategorySpinnerModel3 = new CategorySpinnerModel("2", "Pet Shop");
        CategorySpinnerModelArrayList.add(CategorySpinnerModel3);

        CategorySpinnerModel CategorySpinnerModel4 = new CategorySpinnerModel("3", "Restaurants");
        CategorySpinnerModelArrayList.add(CategorySpinnerModel4);

        CategorySpinnerModel CategorySpinnerModel5 = new CategorySpinnerModel("4", "Gift Shop");
        CategorySpinnerModelArrayList.add(CategorySpinnerModel5);

        CategorySpinnerModel CategorySpinnerModel6 = new CategorySpinnerModel("5", "Health");
        CategorySpinnerModelArrayList.add(CategorySpinnerModel6);

        CategorySpinnerModel CategorySpinnerModel7 = new CategorySpinnerModel("6", "Electronics");
        CategorySpinnerModelArrayList.add(CategorySpinnerModel7);

        CategorySpinnerModel CategorySpinnerModel8 = new CategorySpinnerModel("7", "Pan Shop");
        CategorySpinnerModelArrayList.add(CategorySpinnerModel8);
    }

    public int getImage()
    {
        switch (getId())
        {
            case "0":
                return R.drawable.ic_tap;
            case "1":
                return R.drawable.ic_grocery;
            case "2":
                return R.drawable.ic_pet;
            case "3":
                return R.drawable.ic_restaurants;
            case "4":
                return R.drawable.ic_gift;
            case "5":
                return R.drawable.ic_health;
            case "6":
                return R.drawable.ic_electronic;
            case "7":
                return R.drawable.ic_cigarette;
        }
        return R.drawable.ic_tap;
    }

    public static ArrayList<CategorySpinnerModel> getCategorySpinnerModelArrayList()
    {
        return CategorySpinnerModelArrayList;
    }
}
