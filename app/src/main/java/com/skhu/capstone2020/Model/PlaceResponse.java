package com.skhu.capstone2020.Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlaceResponse {
    @SerializedName("documents")
    private List<Place> placeList;

    public PlaceResponse() {
    }

    public PlaceResponse(List<Place> placeList) {
        this.placeList = placeList;
    }

    public List<Place> getPlaceList() {
        return placeList;
    }

    public void setPlaceList(List<Place> placeList) {
        this.placeList = placeList;
    }
}
