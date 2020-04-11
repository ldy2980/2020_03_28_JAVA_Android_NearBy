package com.skhu.capstone2020.Model;

public class UserOptions {
    private boolean allowNotification = true;
    private boolean allowFriendRequest = true;
    private boolean allowShareLocation = true;

    public UserOptions() {
    }

    public UserOptions(boolean allowNotification, boolean allowFriendRequest, boolean allowShareLocation) {
        this.allowNotification = allowNotification;
        this.allowFriendRequest = allowFriendRequest;
        this.allowShareLocation = allowShareLocation;
    }

    public boolean isAllowNotification() {
        return allowNotification;
    }

    public void setAllowNotification(boolean allowNotification) {
        this.allowNotification = allowNotification;
    }

    public boolean isAllowFriendRequest() {
        return allowFriendRequest;
    }

    public void setAllowFriendRequest(boolean allowFriendRequest) {
        this.allowFriendRequest = allowFriendRequest;
    }

    public boolean isAllowShareLocation() {
        return allowShareLocation;
    }

    public void setAllowShareLocation(boolean allowShareLocation) {
        this.allowShareLocation = allowShareLocation;
    }
}
