package com.skhu.capstone2020.Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AddressResponse {
    @SerializedName("documents")
    private List<AddressDocuments> addressDocuments;

    public AddressResponse() {
    }

    public AddressResponse(List<AddressDocuments> addressDocuments) {
        this.addressDocuments = addressDocuments;
    }

    public List<AddressDocuments> getAddressDocuments() {
        return addressDocuments;
    }

    public void setAddressDocuments(List<AddressDocuments> addressDocuments) {
        this.addressDocuments = addressDocuments;
    }
}
