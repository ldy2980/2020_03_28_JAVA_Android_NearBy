package com.skhu.capstone2020.Model;

import java.util.Date;

public class Chat {
    private String userId;
    private String userName;
    private String imageUrl;
    private String message;
    private Date timeStamp;

    public Chat() {
    }

    public Chat(String userId, String userName, String imageUrl, String message, Date timeStamp) {
        this.userId = userId;
        this.userName = userName;
        this.imageUrl = imageUrl;
        this.message = message;
        this.timeStamp = timeStamp;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
}
