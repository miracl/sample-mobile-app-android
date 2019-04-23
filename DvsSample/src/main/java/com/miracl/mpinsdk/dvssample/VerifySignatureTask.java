package com.miracl.mpinsdk.dvssample;

import android.os.AsyncTask;

import com.miracl.mpinsdk.dvssample.rest.Client;
import com.miracl.mpinsdk.dvssample.rest.DvsServiceApi;
import com.miracl.mpinsdk.dvssample.rest.model.VerifySignatureInfo;
import com.miracl.mpinsdk.model.Status;

import java.io.IOException;

import retrofit2.Response;
import retrofit2.Retrofit;

class VerifySignatureTask extends AsyncTask<Void, Void, Status> {

    private static final int HTTP_CODE_OK = 200;

    private final String mVerificationData;
    private final String mDocumentData;
    private final String mBaseServiceUrl;
    private final VerifySignatureTask.Callback mCallback;
    private VerifySignatureInfo mResponse;

    public VerifySignatureTask(String baseServiceUrl, String verificationData, String documentData, VerifySignatureTask.Callback callback) {
        mBaseServiceUrl = baseServiceUrl;
        mVerificationData = verificationData;
        mDocumentData = documentData;
        mCallback = callback;
    }

    @Override
    protected com.miracl.mpinsdk.model.Status doInBackground(Void... voids) {
        Retrofit retrofit = Client.getClient(mBaseServiceUrl);
        DvsServiceApi dvsServiceApi = retrofit.create(DvsServiceApi.class);

        try {
            Response<VerifySignatureInfo> verifySignatureInfoResponse = dvsServiceApi.verifySignature(mVerificationData, mDocumentData).execute();
            if (verifySignatureInfoResponse.code() == HTTP_CODE_OK) {
                mResponse = verifySignatureInfoResponse.body();
                return new com.miracl.mpinsdk.model.Status(com.miracl.mpinsdk.model.Status.Code.OK, "");
            }
        } catch (IOException e) {
            return new com.miracl.mpinsdk.model.Status(com.miracl.mpinsdk.model.Status.Code.HTTP_REQUEST_ERROR,
                    "Failed to validate signature");
        }

        return new com.miracl.mpinsdk.model.Status(com.miracl.mpinsdk.model.Status.Code.HTTP_REQUEST_ERROR,
                "Failed to validate signature");
    }

    @Override
    protected void onPostExecute(com.miracl.mpinsdk.model.Status status) {

        if (status.getStatusCode() == com.miracl.mpinsdk.model.Status.Code.OK) {
            if (mCallback != null) {
                mCallback.onSuccess(mResponse);
            }
        } else if (mCallback != null) {
            mCallback.onFail(status);
        }
    }

    public interface Callback {

        void onSuccess(VerifySignatureInfo verifySignatureInfo);

        void onFail(com.miracl.mpinsdk.model.Status status);
    }
}
