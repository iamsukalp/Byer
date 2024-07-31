package com.byer.byer.Activities;

import java.util.ArrayList;

public class OrderStatusModel {
    private String tv_status;
    private String tv_orderstatus_time;
    private Boolean completed;

    public OrderStatusModel(String tv_status, String tv_orderstatus_time, Boolean completed) {
        this.tv_status = tv_status;
        this.tv_orderstatus_time = tv_orderstatus_time;
        this.completed = completed;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public String getTv_status() {
        return tv_status;
    }

    public void setTv_status(String tv_status) {
        this.tv_status = tv_status;
    }

    public String getTv_orderstatus_time() {
        return tv_orderstatus_time;
    }

    public void setTv_orderstatus_time(String tv_orderstatus_time) {
        this.tv_orderstatus_time = tv_orderstatus_time;
    }

    public static ArrayList<OrderStatusModel> getStoreDetail() {
        ArrayList<OrderStatusModel> status = new ArrayList<OrderStatusModel>();
        status.add(new OrderStatusModel("Order Rcived", "8:30am,Jan 31,2018",true));
        status.add(new OrderStatusModel("On The Way", "10:30am,Jan 31,2018",true));
        status.add(new OrderStatusModel("Delivered", "aaaaaa",false));
        return status;
    }
}
