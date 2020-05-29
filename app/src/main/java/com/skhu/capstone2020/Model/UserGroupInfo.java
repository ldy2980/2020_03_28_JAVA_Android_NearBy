package com.skhu.capstone2020.Model;

public class UserGroupInfo {
    private String groupId;
    private String masterId;

    public UserGroupInfo() {
    }

    public UserGroupInfo(String groupId, String masterId) {
        this.groupId = groupId;
        this.masterId = masterId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }
}
