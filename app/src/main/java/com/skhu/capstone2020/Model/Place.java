package com.skhu.capstone2020.Model;

import com.google.gson.annotations.SerializedName;

public class Place {
    @SerializedName("id")
    private String placeId;

    @SerializedName("place_name")
    private String placeName;

    @SerializedName("category_name")
    private String category;

    @SerializedName("category_group_name")
    private String groupCategory;

    @SerializedName("phone")
    private String phone;

    @SerializedName("place_url")
    private String url;

    @SerializedName("road_address_name")
    private String address;

    @SerializedName("distance")
    private String distance;

    @SerializedName("x")
    private String x;

    @SerializedName("y")
    private String y;

    public Place() {
    }

    public Place(String placeId, String placeName, String category, String groupCategory, String phone, String url, String address, String distance, String x, String y) {
        this.placeId = placeId;
        this.placeName = placeName;
        this.category = category;
        this.groupCategory = groupCategory;
        this.phone = phone;
        this.url = url;
        this.address = address;
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
