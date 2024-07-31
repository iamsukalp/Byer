package com.flashotech.byerconsole.Models;

public class NotificationModel {
    String name;
    String date;
    boolean solved;
    String raisedBy;


    public NotificationModel(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    public String getRaisedBy() {
        return raisedBy;
    }

    public void setRaisedBy(String raisedBy) {
        this.raisedBy = raisedBy;
    }

    public NotificationModel(String name, String date, boolean solved, String raisedBy) {
        this.name = name;
        this.date = date;
        this.solved = solved;
        this.raisedBy = raisedBy;
    }
}
