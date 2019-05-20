package com.miracl.mpinsdk.mobileapploginsample.rest.model;

import com.google.gson.annotations.SerializedName;

public class AuthCodeInfo {

    @SerializedName("code")
    private String authCode;

    @SerializedName("userID")
    private String userId;

    public AuthCodeInfo(String authCode, String userId) {
        this.authCode = authCode;
        this.userId = userId;
    }
}
