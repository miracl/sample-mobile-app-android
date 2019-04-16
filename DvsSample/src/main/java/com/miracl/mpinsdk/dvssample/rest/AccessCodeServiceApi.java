package com.miracl.mpinsdk.dvssample.rest;

import com.miracl.mpinsdk.dvssample.rest.model.AccessCodeInfo;
import com.miracl.mpinsdk.dvssample.rest.model.AuthorizeUrlInfo;

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
     * POST request to validate user access code
     *
     * @param body AccessCodeInfo
     */
    @Headers("Content-type: application/json")
    @POST("/authtoken")
    Call<ResponseBody> setAuthToken(@Body AccessCodeInfo body);
}
