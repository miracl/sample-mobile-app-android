package com.miracl.mpinsdk.dvssample.rest.model;

import com.google.gson.annotations.SerializedName;

public class AccessCodeInfo {

    @SerializedName("code")
    private final String accessCode;

    @SerializedName("userID")
    private final String userId;

    public AccessCodeInfo(String accessCode, String userId) {
        this.accessCode = accessCode;
        this.userId = userId;
    }
}
