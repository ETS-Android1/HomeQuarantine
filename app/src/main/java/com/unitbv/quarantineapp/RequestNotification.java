package com.unitbv.quarantineapp;

import com.google.gson.annotations.SerializedName;

public class RequestNotification {
    @SerializedName("to") //  "to" changed to token
    private String token;

    @SerializedName("notification")
    private SendNotification sendNotification;

    @SerializedName("data")
    private SendData sendData;

    public SendData getSendData() {
        return sendData;
    }

    public void setSendData(SendData sendData) {
        this.sendData = sendData;
    }

    public SendNotification getSendNotification() {
        return sendNotification;
    }

    public void setSendNotification(SendNotification sendNotification) {
        this.sendNotification = sendNotification;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
