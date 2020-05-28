package com.skhu.capstone2020.Model;

import java.util.Date;

public class RequestNotification extends Notification {
    private String userName;
    private String userImageUrl;
    private String userStatusMessage;
    private Date time;

    public RequestNotification() {
    }

    public RequestNotification(String userId, String userName, String userImageUrl, String userStatusMessage, Date time) {
        super(userId, 0);
        this.userName = userName;
        this.userImageUrl = userImageUrl;
        this.userStatusMessage = userStatusMessage;
        this.time = time;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImageUrl() {
        return userImageUrl;
    }

    public void setUserImageUrl(String userImageUrl) {
        this.userImageUrl = userImageUrl;
    }

    public String getUserStatusMessage() {
        return userStatusMessage;
    }

    public void setUserStatusMessage(String userStatusMessage) {
        this.userStatusMessage = userStatusMessage;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
