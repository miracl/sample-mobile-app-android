package com.miracl.mpinsdk.dvssample.rest;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Client {

    private static Retrofit sRetrofit = null;

    public static Retrofit getClient(String baseServiceUrl) {
        if (sRetrofit == null) {
            sRetrofit = new Retrofit.Builder()
                    .client(new OkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create(new Gson()))
                    .baseUrl(baseServiceUrl)
                    .build();
        }

        return sRetrofit;
    }
}
