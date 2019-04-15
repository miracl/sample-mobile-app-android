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

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

}
