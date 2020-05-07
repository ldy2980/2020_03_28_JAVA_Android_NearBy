package com.skhu.capstone2020.Model;

import com.google.gson.annotations.SerializedName;

public class Sender {
    @SerializedName("data")
    private Data data;

    @SerializedName("to")
    private String to;

    public Sender() {
    }

    public Sender(String to, Data data) {
        this.data = data;
        this.to = to;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
