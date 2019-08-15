package com.miracl.mpinsdk.dvssample;

import android.app.AlertDialog;
import android.content.Context;
import androidx.annotation.NonNull;

import com.miracl.mpinsdk.model.Status;

class MessageDialog extends AlertDialog {

    private String mMessage;

    public MessageDialog(@NonNull Context context) {
        super(context);
        init();
    }

    @Override
    public void show() {
        if (mMessage != null) {
            setMessage(mMessage);
        }
        super.show();
    }

    public void show(Status status) {
        setMessage(status);
        show();
    }

    public void show(String message) {
        mMessage = message;
        show();
    }


    private void setMessage(Status status) {
        if (status != null) {
            mMessage = "Error status code: " + status.getStatusCode() + "\nError message: " +
                    status.getErrorMessage();
        }
    }

    private void init() {
        setButton(BUTTON_POSITIVE, "OK", (OnClickListener) null);
    }
}
