package com.miracl.mpinsdk.dvssample;

import android.os.AsyncTask;

import com.miracl.mpinsdk.dvssample.rest.AccessCodeServiceApi;
import com.miracl.mpinsdk.dvssample.rest.Client;
import com.miracl.mpinsdk.dvssample.rest.model.AuthCodeInfo;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Just an example of how a validation with auth code can be done with a demo service. Actual implementations can be different and
 * more robust.
 */
class ValidateLoginTask extends AsyncTask<Void, Void, Boolean> {

    private static final int HTTP_CODE_OK = 200;

    private final String mBaseServiceUrl;
    private final String mAuthCode;
    private final String mUserId;
    private final ValidationListener mListener;

    public ValidateLoginTask(String baseServiceUrl, String authCode, String userId, ValidationListener listener) {
        mBaseServiceUrl = baseServiceUrl;
        mAuthCode = authCode;
        mUserId = userId;
        mListener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        Retrofit retrofit = Client.getClient(mBaseServiceUrl);
        AccessCodeServiceApi accessCodeServiceApi = retrofit.create(AccessCodeServiceApi.class);

        try {
            Response<ResponseBody> responseSetAuthToken = accessCodeServiceApi
                    .setAuthToken(new AuthCodeInfo(mAuthCode, mUserId)).execute();

            return responseSetAuthToken.code() == HTTP_CODE_OK;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean isSuccessful) {
        super.onPostExecute(isSuccessful);
        if (mListener != null) {
            mListener.onValidate(isSuccessful);
        }
    }

    public interface ValidationListener {

        void onValidate(boolean isSuccessful);
    }
}
