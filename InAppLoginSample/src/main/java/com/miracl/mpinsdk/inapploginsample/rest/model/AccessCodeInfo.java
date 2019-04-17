package com.miracl.mpinsdk.inapploginsample.rest.model;

import com.google.gson.annotations.SerializedName;

public class AccessCodeInfo {

    @SerializedName("code")
    private String accessCode;

    @SerializedName("userID")
    private String userId;

    public AccessCodeInfo(String accessCode, String userId) {
        this.accessCode = accessCode;
        this.userId = userId;
    }
}
