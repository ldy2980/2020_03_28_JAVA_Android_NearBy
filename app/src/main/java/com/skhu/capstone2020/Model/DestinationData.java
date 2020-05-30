package com.skhu.capstone2020.Model;

import com.google.gson.annotations.SerializedName;

public class DestinationData {
    @SerializedName("masterId")
    private String masterId;

    @SerializedName("groupId")
    private String groupId;

    @SerializedName("groupName")
    private String groupName;

    @SerializedName("placeId")
    private String placeId;

    @SerializedName("placeName")
    private String placeName;

    @SerializedName("memberId")
    private String memberId;

    public DestinationData() {
    }

    public DestinationData(String masterId, String groupId, String groupName, String placeId, String placeName, String memberId) {
        this.masterId = masterId;
        this.groupId = groupId;
        this.groupName = groupName;
        this.placeId = placeId;
        this.placeName = placeName;
        this.memberId = memberId;
    }

    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
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

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }
}
