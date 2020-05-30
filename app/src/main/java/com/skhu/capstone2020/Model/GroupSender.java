package com.skhu.capstone2020.Model;

import com.google.gson.annotations.SerializedName;

public class GroupSender {
    @SerializedName("data")
    private DestinationData destinationData;

    @SerializedName("to")
    private String token;

    public GroupSender() {
    }

    public GroupSender(DestinationData destinationData, String token) {
        this.destinationData = destinationData;
        this.token = token;
    }

    public DestinationData getDestinationData() {
        return destinationData;
    }

    public void setDestinationData(DestinationData destinationData) {
        this.destinationData = destinationData;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
