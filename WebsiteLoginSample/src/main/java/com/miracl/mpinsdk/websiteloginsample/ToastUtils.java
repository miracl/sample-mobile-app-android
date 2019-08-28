package com.miracl.mpinsdk.websiteloginsample;

import android.app.Activity;
import androidx.annotation.NonNull;
import android.widget.Toast;

import com.miracl.mpinsdk.model.Status;

public class ToastUtils {

    public static void showStatus(final @NonNull Activity activity, final @NonNull Status status) {
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(activity, "Status code: " + status.getStatusCode() + " message: " + status.getErrorMessage(),
                  Toast.LENGTH_SHORT).show();
            }
        });
    }
}
