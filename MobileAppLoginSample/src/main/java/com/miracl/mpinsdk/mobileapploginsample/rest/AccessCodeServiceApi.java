package com.miracl.mpinsdk.mobileapploginsample.rest;

import com.miracl.mpinsdk.mobileapploginsample.rest.model.AuthCodeInfo;
import com.miracl.mpinsdk.mobileapploginsample.rest.model.AuthorizeUrlInfo;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
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

    /**
     * POST request to validate user auth code
     *
     * @param body
     *   AuthCodeInfo
     */
    @Headers("Content-type: application/json")
    @POST("/authtoken")
    Call<ResponseBody> setAuthToken(@Body AuthCodeInfo body);
}
