package com.miracl.mpinsdk.dvssample;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.miracl.mpinsdk.MPinMfaAsync;
import com.miracl.mpinsdk.model.Status;

public class SampleApplication extends Application {

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
