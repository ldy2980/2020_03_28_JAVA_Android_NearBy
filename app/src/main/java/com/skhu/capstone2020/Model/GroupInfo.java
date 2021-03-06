package com.skhu.capstone2020.Model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class GroupInfo implements Serializable {
    private String masterId;
    private String groupName;
    private String groupId;
    private String lastMessage;
    private Date lastMessageTime;
    private int count;
    private List<Member> memberList;
    private boolean setDestination = false;

    public GroupInfo() {
    }

    public GroupInfo(String masterId, String groupName, String groupId, String lastMessage, Date lastMessageTime, int count, List<Member> memberList) {
        this.masterId = masterId;
        this.groupName = groupName;
        this.groupId = groupId;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.count = count;
        this.memberList = memberList;
    }

    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Date getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(Date lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Member> getMemberList() {
        return memberList;
    }

    public void setMemberList(List<Member> memberList) {
        this.memberList = memberList;
    }

    public boolean isSetDestination() {
        return setDestination;
    }

    public void setSetDestination(boolean setDestination) {
        this.setDestination = setDestination;
    }
}