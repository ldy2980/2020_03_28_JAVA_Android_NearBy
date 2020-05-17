package com.skhu.capstone2020.Model.PlaceResponse;

import com.google.gson.annotations.SerializedName;

public class Meta {
    @SerializedName("total_count")
    private int totalCount;

    @SerializedName("pageable_count")
    private int pageableCount;

    @SerializedName("is_end")
    private boolean isEnd;

    @SerializedName("same_name")
    private SameName sameName;

    public Meta() {
    }

    public Meta(int totalCount, int pageableCount, boolean isEnd, SameName sameName) {
        this.totalCount = totalCount;
        this.pageableCount = pageableCount;
        this.isEnd = isEnd;
        this.sameName = sameName;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getPageableCount() {
        return pageableCount;
    }

    public void setPageableCount(int pageableCount) {
        this.pageableCount = pageableCount;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean end) {
        isEnd = end;
    }

    public SameName getSameName() {
        return sameName;
    }

    public void setSameName(SameName sameName) {
        this.sameName = sameName;
    }
}
