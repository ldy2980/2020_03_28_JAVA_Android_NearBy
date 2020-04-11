package com.skhu.capstone2020.Model;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {
    private String id;
    private String name;
    private String email;
    private String imageUrl;
    private String statusMessage;
    private List<String> friendIdList;
    private boolean allowNotification = true;
    private boolean allowFriendRequest = true;
    private boolean allowShareLocation = true;

    public User() {
    }

    public User(String id, String name, String email, String imageUrl) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public List<String> getFriendIdList() {
        return friendIdList;
    }

    public void setFriendIdList(List<String> friendIdList) {
        this.friendIdList = friendIdList;
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
