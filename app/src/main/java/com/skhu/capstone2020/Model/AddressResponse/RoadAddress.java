package com.skhu.capstone2020.Model.AddressResponse;

import com.google.gson.annotations.SerializedName;

public class RoadAddress {
    @SerializedName("address_name")
    private String address_name;

    @SerializedName("region_1depth_name")
    private String region_1depth_name;

    @SerializedName("region_2depth_name")
    private String region_2depth_name;

    @SerializedName("region_3depth_name")
    private String region_3depth_name;

    @SerializedName("road_name")
    private String road_name;

    @SerializedName("underground_yn")
    private String underground_yn;

    @SerializedName("main_building_no")
    private String main_building_no;

    @SerializedName("sub_building_no")
    private String sub_building_no;

    @SerializedName("building_name")
    private String building_name;

    @SerializedName("zone_no")
    private String zone_no;

    public RoadAddress() {
    }

    public RoadAddress(String address_name,
                       String region_1depth_name,
                       String region_2depth_name,
                       String region_3depth_name,
                       String road_name,
                       String underground_yn,
                       String main_building_no,
                       String sub_building_no,
                       String building_name,
                       String zone_no) {
        this.address_name = address_name;
        this.region_1depth_name = region_1depth_name;
        this.region_2depth_name = region_2depth_name;
        this.region_3depth_name = region_3depth_name;
        this.road_name = road_name;
        this.underground_yn = underground_yn;
        this.main_building_no = main_building_no;
        this.sub_building_no = sub_building_no;
        this.building_name = building_name;
        this.zone_no = zone_no;
    }

    public String getAddress_name() {
        return address_name;
    }

    public void setAddress_name(String address_name) {
        this.address_name = address_name;
    }

    public String getRegion_1depth_name() {
        return region_1depth_name;
    }

    public void setRegion_1depth_name(String region_1depth_name) {
        this.region_1depth_name = region_1depth_name;
    }

    public String getRegion_2depth_name() {
        return region_2depth_name;
    }

    public void setRegion_2depth_name(String region_2depth_name) {
        this.region_2depth_name = region_2depth_name;
    }

    public String getRegion_3depth_name() {
        return region_3depth_name;
    }

    public void setRegion_3depth_name(String region_3depth_name) {
        this.region_3depth_name = region_3depth_name;
    }

    public String getRoad_name() {
        return road_name;
    }

    public void setRoad_name(String road_name) {
        this.road_name = road_name;
    }

    public String getUnderground_yn() {
        return underground_yn;
    }

    public void setUnderground_yn(String underground_yn) {
        this.underground_yn = underground_yn;
    }

    public String getMain_building_no() {
        return main_building_no;
    }

    public void setMain_building_no(String main_building_no) {
        this.main_building_no = main_building_no;
    }

    public String getSub_building_no() {
        return sub_building_no;
    }

    public void setSub_building_no(String sub_building_no) {
        this.sub_building_no = sub_building_no;
    }

    public String getBuilding_name() {
        return building_name;
    }

    public void setBuilding_name(String building_name) {
        this.building_name = building_name;
    }

    public String getZone_no() {
        return zone_no;
    }

    public void setZone_no(String zone_no) {
        this.zone_no = zone_no;
    }
}
