package com.miracl.mpinsdk.bootstrapsample.rest;

import com.miracl.mpinsdk.bootstrapsample.rest.model.AuthorizeUrlInfo;

import retrofit2.Call;
import retrofit2.http.POST;

/**
 * REST Controller
 */
public interface AccessCodeServiceApi {

    /**
     * GET request for authorization URL
     *
     * @return ServiceConfiguration
     */
    @POST("/authzurl")
    Call<AuthorizeUrlInfo> getAuthURL();
}
