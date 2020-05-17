package com.skhu.capstone2020.Model.PlaceResponse;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlaceResponse {
    @SerializedName("meta")
    private Meta meta;

    @SerializedName("documents")
    private List<Place> placeList;

    public PlaceResponse() {
    }

    public PlaceResponse(List<Place> placeList) {
        this.placeList = placeList;
    }

    public PlaceResponse(Meta meta, List<Place> placeList) {
        this.meta = meta;
        this.placeList = placeList;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public List<Place> getPlaceList() {
        return placeList;
    }

    public void setPlaceList(List<Place> placeList) {
        this.placeList = placeList;
    }
}
