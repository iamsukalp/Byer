package com.byer.byer.Models;

public class StatusModel {

    String image;
    String status;
    long timestamp;

    public StatusModel(){}

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public StatusModel(String image, String status, long timestamp) {
        this.image = image;
        this.status = status;
        this.timestamp = timestamp;
    }
}
