package com.skhu.capstone2020.Model;

import com.google.gson.annotations.SerializedName;

public class Sender {
    @SerializedName("data")
    private Data data;

    @SerializedName("to")
    private String token;

    public Sender() {
    }

    public Sender(Data data, String token) {
        this.data = data;
        this.token = token;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
