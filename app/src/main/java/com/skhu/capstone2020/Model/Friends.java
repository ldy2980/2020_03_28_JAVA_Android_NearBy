package com.skhu.capstone2020.Model;

import java.util.List;

public class Friends {
    private List<User> friendList;

    public Friends() {
    }

    public Friends(List<User> friendList) {
        this.friendList = friendList;
    }

    public List<User> getFriendList() {
        return friendList;
    }

    public void setFriendList(List<User> friendList) {
        this.friendList = friendList;
    }
}
