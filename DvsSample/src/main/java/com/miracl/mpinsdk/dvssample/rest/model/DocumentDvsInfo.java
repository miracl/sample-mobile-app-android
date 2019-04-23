package com.miracl.mpinsdk.dvssample.rest.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DocumentDvsInfo implements Serializable {

    @SerializedName("timestamp")
    private Long timestamp;

    @SerializedName("hash")
    private String hash;

    @SerializedName("authToken")
    private String authToken;

    public Long getTimestamp() {
        return timestamp;
    }

    public String getHash() {
        return hash;
    }

    public String getAuthToken() {
        return authToken;
    }
}
