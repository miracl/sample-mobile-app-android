package com.miracl.mpinsdk.bootstrapsample.rest.model;

import com.google.gson.annotations.SerializedName;

public class AuthorizeUrlInfo {

    @SerializedName("authorizeURL")
    private String authorizeUrl;

    public String getAuthorizeUrl() {
        return authorizeUrl;
    }
}
