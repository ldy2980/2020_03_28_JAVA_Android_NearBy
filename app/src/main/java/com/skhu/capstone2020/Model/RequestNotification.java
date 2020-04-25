package com.skhu.capstone2020.Model;

public class RequestNotification extends Notification {
    private String userName;
    private String userImageUrl;
    private String userStatusMessage;
    private String time;

    public RequestNotification() {
    }

    public RequestNotification(String userId, String userName, String userImageUrl, String userStatusMessage, String time) {
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
