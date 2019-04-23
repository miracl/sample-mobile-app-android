package com.miracl.mpinsdk.dvssample;

import android.os.AsyncTask;

import com.miracl.mpinsdk.dvssample.rest.Client;
import com.miracl.mpinsdk.dvssample.rest.DvsServiceApi;
import com.miracl.mpinsdk.dvssample.rest.model.DocumentDvsInfo;
import com.miracl.mpinsdk.model.Status;

import java.io.IOException;

import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * An example of how a document hash can be created with a demo service.
 */
class CreateDocumentHashTask extends AsyncTask<Void, Void, Status> {
    private static final int HTTP_CODE_OK = 200;

    private final String mDocument;
    private final String mBaseServiceUrl;
    private final CreateDocumentHashTask.Callback mCallback;

    private DocumentDvsInfo mResponse;

    public CreateDocumentHashTask(String baseServiceUrl, String document, CreateDocumentHashTask.Callback callback) {
        mBaseServiceUrl = baseServiceUrl;
        mDocument = document;
        mCallback = callback;
    }

    @Override
    protected com.miracl.mpinsdk.model.Status doInBackground(Void... voids) {
        Retrofit retrofit = Client.getClient(mBaseServiceUrl);
        DvsServiceApi dvsServiceApi = retrofit.create(DvsServiceApi.class);

        try {
            Response<DocumentDvsInfo> documentDvsInfoResponse = dvsServiceApi.createDocumentHash(mDocument).execute();
            if (documentDvsInfoResponse.code() == HTTP_CODE_OK) {
                mResponse = documentDvsInfoResponse.body();
                return new com.miracl.mpinsdk.model.Status(com.miracl.mpinsdk.model.Status.Code.OK, "");
            }
        } catch (IOException e) {
            return new com.miracl.mpinsdk.model.Status(com.miracl.mpinsdk.model.Status.Code.HTTP_REQUEST_ERROR,
                    "Failed to create document hash");
        }

        return new com.miracl.mpinsdk.model.Status(com.miracl.mpinsdk.model.Status.Code.HTTP_REQUEST_ERROR,
                "Failed to create document hash");
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

        void onSuccess(DocumentDvsInfo documentDvsInfo);

        void onFail(com.miracl.mpinsdk.model.Status status);
    }
}

