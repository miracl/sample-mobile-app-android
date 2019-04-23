package com.miracl.mpinsdk.websiteloginsample;

import android.app.Application;

import com.miracl.mpinsdk.MPinMfaAsync;

public class SampleApplication extends Application {


    private static MPinMfaAsync sMPinMfa;
    private static String       sAccessCode;

    @Override
    public void onCreate() {
        super.onCreate();
        sMPinMfa = new MPinMfaAsync(this);
        sMPinMfa.init(this, getString(R.string.mpin_cid), null, null);
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
