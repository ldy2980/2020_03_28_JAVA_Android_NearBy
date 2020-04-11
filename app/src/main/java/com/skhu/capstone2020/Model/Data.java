package com.skhu.capstone2020.Model;

public class Data {
    private String userId;
    private String userName;
    private String receiverId;

    public Data() {
    }

    public Data(String userId, String userName, String receiverId) {
        this.userId = userId;
        this.userName = userName;
        this.receiverId = receiverId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }
}
