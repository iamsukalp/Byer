package com.byer.byerretailer.Models;

public class StatusModel {
    String title;
    String message;
    long serial;
    long viewType;
    String image;
    String party;


    public StatusModel(){}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public long getSerial() {
        return serial;
    }

    public void setSerial(long serial) {
        this.serial = serial;
    }

    public long getViewType() {
        return viewType;
    }

    public void setViewType(long viewType) {
        this.viewType = viewType;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public StatusModel(String title, String message, long serial, long viewType, String image, String party) {
        this.title = title;
        this.message = message;
        this.serial = serial;
        this.viewType = viewType;
        this.image = image;
        this.party = party;
    }

    public String getParty() {
        return party;
    }

    public void setParty(String party) {
        this.party = party;
    }
}
