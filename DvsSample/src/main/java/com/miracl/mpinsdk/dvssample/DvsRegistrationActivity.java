package com.miracl.mpinsdk.dvssample;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.miracl.mpinsdk.MPinMfaAsync;
import com.miracl.mpinsdk.model.Status;

public class DvsRegistrationActivity extends AppCompatActivity implements EnterPinDialog.EventListener {

    private EnterPinDialog mEnterPinDialog;
    private MessageDialog mMessageDialog;

    private boolean registrationStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dvs_registration);

        mEnterPinDialog = new EnterPinDialog(this, this);
        mMessageDialog = new MessageDialog(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }


    @Override
    public void onPinEntered(final String pin) {
        // Check the registration state and continue the registration process with the provided pin
        if (!registrationStarted) {
            startRegistrationDvs(pin);
        } else {
            finishRegistrationDvs(pin);
        }
    }

    @Override
    public void onPinCanceled() {

    }

    private void startRegistrationDvs(String pin) {
        // Start the DVS registration process
        SampleApplication.getMfaSdk().startRegistrationDvs(SampleApplication.getLoggedUser(),
                new String[]{pin},
                new MPinMfaAsync.Callback<Void>() {

                    @Override
                    protected void onSuccess(@Nullable Void result) {
                        // The DVS registration process is started successfully
                        registrationStarted = true;
                        mEnterPinDialog = new EnterPinDialog(DvsRegistrationActivity.this,
                                DvsRegistrationActivity.this);
                        mEnterPinDialog.setTitle(getString(R.string.title_enter_pin_for_signing));
                        mEnterPinDialog.show();
                    }

                    @Override
                    protected void onFail(final @NonNull Status status) {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                // Starting DVS registration has failed
                                mMessageDialog.show(status);
                            }
                        });
                    }
                });
    }

    private void finishRegistrationDvs(String pin) {
        // Finish the DVS registration process
        SampleApplication.getMfaSdk().finishRegistrationDvs(SampleApplication.getLoggedUser(),
                new String[]{pin},
                new MPinMfaAsync.Callback<Void>() {

                    @Override
                    protected void onSuccess(@Nullable Void result) {
                        // The DVS registration for the user is complete
                        startActivity(new Intent(DvsRegistrationActivity.this,
                                SignMessageActivity.class));
                        finish();
                    }

                    @Override
                    protected void onFail(final @NonNull Status status) {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                // Finishing DVS registration has failed
                                mMessageDialog.show(status);
                            }
                        });
                    }
                });
    }

    private void init() {
        // Check for logged user
        if (SampleApplication.isUserLogged()) {
            // If there is logged user, init the views
            initViews();
        } else {
            // If there isn't logged user, start LoginActivity
            startActivity(new Intent(DvsRegistrationActivity.this, LoginActivity.class));
        }
    }

    private void onDvsRegistrationClick() {
        mEnterPinDialog.setTitle(SampleApplication.getLoggedUser().getId());
        mEnterPinDialog.show();
    }

    private void initViews() {
        ((TextView) findViewById(R.id.user_id_text)).setText(SampleApplication.getLoggedUser().getId());
        findViewById(R.id.register_dvs_button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                onDvsRegistrationClick();
            }
        });
    }
}
