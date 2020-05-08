package com.skhu.capstone2020.Model;

import com.google.gson.annotations.SerializedName;

public class Data {
    @SerializedName("userId")
    private String userId;

    @SerializedName("userName")
    private String userName;

    @SerializedName("userImage")
    private String userImage;

    @SerializedName("userStatusMessage")
    private String userStatusMessage;

    @SerializedName("receiverId")
    private String receiverId;

    public Data() {
    }

    public Data(String userId, String userName, String userImage, String userStatusMessage, String receiverId) {
        this.userId = userId;
        this.userName = userName;
        this.userImage = userImage;
        this.userStatusMessage = userStatusMessage;
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

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getUserStatusMessage() {
        return userStatusMessage;
    }

    public void setUserStatusMessage(String userStatusMessage) {
        this.userStatusMessage = userStatusMessage;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }
}
