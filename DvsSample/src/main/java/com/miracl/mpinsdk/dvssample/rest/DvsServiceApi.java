package com.miracl.mpinsdk.dvssample.rest;

import com.miracl.mpinsdk.dvssample.rest.model.DocumentDvsInfo;
import com.miracl.mpinsdk.dvssample.rest.model.VerifySignatureInfo;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * REST Controller
 */
public interface DvsServiceApi {
    /**
     * POST request to create document hash
     *
     * @param document String
     * @return DocumentDvsInfo
     */
    @FormUrlEncoded
    @POST("/login/CreateDocumentHash")
    Call<DocumentDvsInfo> createDocumentHash(@Field("document") String document);

    /**
     * POST request to verify document signature
     *
     * @param verificationData String
     * @param documentData     String
     * @return VerifySignature
     */
    @FormUrlEncoded
    @POST("/login/VerifySignature")
    Call<VerifySignatureInfo> verifySignature(@Field("verificationData") String verificationData,
                                              @Field("documentData") String documentData);
}
