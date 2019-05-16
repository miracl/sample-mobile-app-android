package com.miracl.mpinsdk.dvssample.rest.model;

import com.google.gson.annotations.SerializedName;

public class AuthCodeInfo {

    @SerializedName("code")
    private final String authCode;

    @SerializedName("userID")
    private final String userId;

    public AuthCodeInfo(String authCode, String userId) {
        this.authCode = authCode;
        this.userId = userId;
    }
}
