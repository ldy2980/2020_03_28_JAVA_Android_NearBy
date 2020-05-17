package com.skhu.capstone2020.Model.AddressResponse;

import com.google.gson.annotations.SerializedName;

public class AddressDocuments {
    @SerializedName("address")
    private Address address;

    @SerializedName("road_address")
    private RoadAddress roadAddress;

    public AddressDocuments() {
    }

    public AddressDocuments(Address address, RoadAddress roadAddress) {
        this.address = address;
        this.roadAddress = roadAddress;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public RoadAddress getRoadAddress() {
        return roadAddress;
    }

    public void setRoadAddress(RoadAddress roadAddress) {
        this.roadAddress = roadAddress;
    }
}
