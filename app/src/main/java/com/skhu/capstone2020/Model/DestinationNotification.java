package com.skhu.capstone2020.Model;

import java.util.Date;

public class DestinationNotification extends Notification {
    private String groupId;
    private String groupName;
    private String placeId;
    private String placeName;
    private String masterId;
    private Date time;

    public DestinationNotification() {
    }

    public DestinationNotification(String groupId, String groupName, String placeId, String placeName, String masterId, Date time) {
        super(groupId, 1);
        this.groupId = groupId;
        this.groupName = groupName;
        this.placeId = placeId;
        this.placeName = placeName;
        this.masterId = masterId;
        this.time = time;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
