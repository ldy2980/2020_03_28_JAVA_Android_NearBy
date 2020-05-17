package com.skhu.capstone2020.Model.PlaceResponse;

import com.google.gson.annotations.SerializedName;

public class SameName {
    @SerializedName("region")
    private String[] region;

    @SerializedName("keyword")
    private String keyword;

    @SerializedName("selected_region")
    private String selectedRegion;

    public SameName() {
    }

    public SameName(String[] region, String keyword, String selectedRegion) {
        this.region = region;
        this.keyword = keyword;
        this.selectedRegion = selectedRegion;
    }

    public String[] getRegion() {
        return region;
    }

    public void setRegion(String[] region) {
        this.region = region;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getSelectedRegion() {
        return selectedRegion;
    }

    public void setSelectedRegion(String selectedRegion) {
        this.selectedRegion = selectedRegion;
    }
}
