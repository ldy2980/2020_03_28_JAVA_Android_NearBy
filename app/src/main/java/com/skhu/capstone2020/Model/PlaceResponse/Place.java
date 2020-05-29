package com.skhu.capstone2020.Model.PlaceResponse;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Place implements Serializable {
    @SerializedName("id")
    private String placeId;

    @SerializedName("place_name")
    private String placeName;

    @SerializedName("category_name")
    private String category;

    @SerializedName("category_group_name")
    private String groupCategory;

    @SerializedName("category_group_code")
    private String categoryCode;

    @SerializedName("phone")
    private String phone;

    @SerializedName("place_url")
    private String url;

    @SerializedName("address_name")
    private String address;

    @SerializedName("road_address_name")
    private String roadAddress;

    @SerializedName("distance")
    private String distance;

    @SerializedName("x")
    private String x;

    @SerializedName("y")
    private String y;

    public Place() {
    }

    public Place(String placeId, String placeName, String category, String groupCategory, String categoryCode, String phone, String url, String address, String roadAddress, String distance, String x, String y) {
        this.placeId = placeId;
        this.placeName = placeName;
        this.category = category;
        this.groupCategory = groupCategory;
        this.categoryCode = categoryCode;
        this.phone = phone;
        this.url = url;
        this.address = address;
        this.roadAddress = roadAddress;
        this.distance = distance;
        this.x = x;
        this.y = y;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getGroupCategory() {
        return groupCategory;
    }

    public void setGroupCategory(String groupCategory) {
        this.groupCategory = groupCategory;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRoadAddress() {
        return roadAddress;
    }

    public void setRoadAddress(String roadAddress) {
        this.roadAddress = roadAddress;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }
}
