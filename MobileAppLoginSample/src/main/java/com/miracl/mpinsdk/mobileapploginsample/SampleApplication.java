package com.miracl.mpinsdk.mobileapploginsample;

import android.app.Application;

import com.miracl.mpinsdk.MPinMfaAsync;

public class SampleApplication extends Application {

    static {
        // We need to load the MPinSDK lib
        System.loadLibrary("AndroidMpinSDK");
    }

    private static MPinMfaAsync sMPinMfa;
    private static String       sAccessCode;

    @Override
    public void onCreate() {
        super.onCreate();
        // Init the MPinMfa without additional configuration
        sMPinMfa = new MPinMfaAsync(this);
        sMPinMfa.init(this, null);
    }

    public static MPinMfaAsync getMfaSdk() {
        return sMPinMfa;
    }

    public static String getCurrentAccessCode() {
        return sAccessCode;
    }

    public static void setCurrentAccessCode(String accessCode) {
        sAccessCode = accessCode;
    }
}
