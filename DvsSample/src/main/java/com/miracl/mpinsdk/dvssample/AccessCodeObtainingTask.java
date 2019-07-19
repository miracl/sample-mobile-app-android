package com.miracl.mpinsdk.dvssample;

import android.os.AsyncTask;

import com.miracl.mpinsdk.MPinMFA;
import com.miracl.mpinsdk.dvssample.rest.AccessCodeServiceApi;
import com.miracl.mpinsdk.dvssample.rest.Client;
import com.miracl.mpinsdk.dvssample.rest.model.AuthorizeUrlInfo;
import com.miracl.mpinsdk.model.Status;

import java.io.IOException;

import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * An example of how an authUrl can be obtained in order to pass it to the SDK and receive and accessCode
 */
class AccessCodeObtainingTask extends AsyncTask<Void, Void, Status> {

    private static final int HTTP_CODE_OK = 200;

    private final String mBaseServiceUrl;
    private final Callback mCallback;

    private String mAccessCode;

    public AccessCodeObtainingTask(String baseServiceUrl, Callback callback) {
        mBaseServiceUrl = baseServiceUrl;
        mCallback = callback;
    }

    @Override
    protected com.miracl.mpinsdk.model.Status doInBackground(Void... voids) {
        MPinMFA mfaSdk = SampleApplication.getMfaSdk().getMfaSdk();
        Retrofit retrofit = Client.getClient(mBaseServiceUrl);

        AccessCodeServiceApi accessCodeServiceApi = retrofit.create(AccessCodeServiceApi.class);

        try {
            // Get the auth url from a demo service
            Response<AuthorizeUrlInfo> responseAuthUrl = accessCodeServiceApi.getAuthURL().execute();
            if (responseAuthUrl.code() == HTTP_CODE_OK && responseAuthUrl.body() != null) {
                AuthorizeUrlInfo urlInfo = responseAuthUrl.body();

                StringBuilder accessCodeContainer = new StringBuilder();
                // Use the auth url in order to receive an access code
                com.miracl.mpinsdk.model.Status status = mfaSdk.getAccessCode(urlInfo.getAuthorizeUrl(), accessCodeContainer);
                if (status.getStatusCode() == com.miracl.mpinsdk.model.Status.Code.OK) {
                    mAccessCode = accessCodeContainer.toString();
                }
                return status;
            }
        } catch (IOException e) {
            return new com.miracl.mpinsdk.model.Status(com.miracl.mpinsdk.model.Status.Code.HTTP_REQUEST_ERROR,
                    "Failed to validate access code");
        }

        return new com.miracl.mpinsdk.model.Status(com.miracl.mpinsdk.model.Status.Code.HTTP_REQUEST_ERROR,
                "Failed to validate access code");
    }

    @Override
    protected void onPostExecute(com.miracl.mpinsdk.model.Status status) {

        if (status.getStatusCode() == com.miracl.mpinsdk.model.Status.Code.OK) {
            SampleApplication.setCurrentAccessCode(mAccessCode);
            if (mCallback != null) {
                mCallback.onSuccess();
            }
        } else if (mCallback != null) {
            mCallback.onFail(status);
        }
    }

    public interface Callback {

        void onSuccess();

        void onFail(com.miracl.mpinsdk.model.Status status);
    }
}
