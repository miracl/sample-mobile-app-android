package com.miracl.mpinsdk.mobileapploginsample.rest.model;

import com.google.gson.annotations.SerializedName;

public class AuthorizeUrlInfo {

    @SerializedName("authorizeURL")
    private String authorizeUrl;

    public String getAuthorizeUrl() {
        return authorizeUrl;
    }
}
