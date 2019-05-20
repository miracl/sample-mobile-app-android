package com.miracl.mpinsdk.mobileapploginsample;

import com.google.gson.Gson;

import android.os.AsyncTask;

import com.miracl.mpinsdk.MPinMFA;
import com.miracl.mpinsdk.mobileapploginsample.rest.AccessCodeServiceApi;
import com.miracl.mpinsdk.mobileapploginsample.rest.model.AuthorizeUrlInfo;
import com.miracl.mpinsdk.model.Status;

import java.io.IOException;

import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * An example of how an authUrl can be obtained in order to pass it to the SDK and receive an accessCode
 */
public class AccessCodeObtainingTask extends AsyncTask<Void, Void, Status> {

    private static final int HTTP_CODE_OK = 200;

    public interface Callback {

        void onSuccess();

        void onFail(com.miracl.mpinsdk.model.Status status);
    }

    private String   mAuthServiceUrl;
    private Callback mCallback;
    private String   mAccessCode;

    public AccessCodeObtainingTask(String authServiceUrl, Callback callback) {
        mAuthServiceUrl = authServiceUrl;
        mCallback = callback;
    }

    @Override
    protected com.miracl.mpinsdk.model.Status doInBackground(Void... voids) {
        MPinMFA mfaSdk = SampleApplication.getMfaSdk().getMfaSdk();
        Retrofit retrofit;
        try {
            retrofit = new Retrofit.Builder().client(new OkHttpClient())
              .addConverterFactory(GsonConverterFactory.create(new Gson())).baseUrl(mAuthServiceUrl).build();
        } catch (IllegalArgumentException exception) {
            return new com.miracl.mpinsdk.model.Status(com.miracl.mpinsdk.model.Status.Code.HTTP_REQUEST_ERROR,
              "Invalid custom service URL");
        }

        AccessCodeServiceApi accessCodeServiceApi = retrofit.create(AccessCodeServiceApi.class);

        try {
            // Get the auth url from a demo service
            Response<AuthorizeUrlInfo> responseAuthUrl = accessCodeServiceApi.getAuthURL().execute();
            if (responseAuthUrl.code() == HTTP_CODE_OK) {
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
}
