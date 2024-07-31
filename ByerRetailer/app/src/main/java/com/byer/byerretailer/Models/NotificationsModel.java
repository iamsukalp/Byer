package com.byer.byerretailer.Models;

public class NotificationsModel {
    String name;
    String date;
    boolean solved;
    boolean seen;

    public NotificationsModel(){}

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

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public NotificationsModel(String name, String date, boolean solved, boolean seen) {
        this.name = name;
        this.date = date;
        this.solved = solved;
        this.seen = seen;
    }
}
