package com.miracl.mpinsdk.dvssample.rest.model;

import com.google.gson.annotations.SerializedName;

public class AuthorizeUrlInfo {

    @SerializedName("authorizeURL")
    private String authorizeUrl;

    public String getAuthorizeUrl() {
        return authorizeUrl;
    }
}
