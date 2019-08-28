package com.miracl.mpinsdk.websiteloginsample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.miracl.mpinsdk.MPinMFA;
import com.miracl.mpinsdk.MPinMfaAsync;
import com.miracl.mpinsdk.model.ServiceDetails;
import com.miracl.mpinsdk.model.SessionDetails;
import com.miracl.mpinsdk.model.Status;
import com.miracl.mpinsdk.model.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class QrReaderActivity extends AppCompatActivity implements EnterPinDialog.EventListener,
        QrReaderFragment.OnQrReaderFragmentInteractionListener {

    private EnterPinDialog mEnterPinDialog;

    private String mCurrentAccessCode;
    private User mCurrentUser;
    private ServiceDetails mCurrentServiceDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_reader);
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, QrReaderFragment.newInstance())
                    .commit();
        }

        mEnterPinDialog = new EnterPinDialog(this, this);
    }

    @Override
    public void onQrResult(String result) {
        Uri qrUri = Uri.parse(result);

        // Check if the url from the qr has the expected parts
        if (qrUri.getScheme() != null && qrUri.getAuthority() != null && qrUri.getFragment() != null && !qrUri.getFragment()
                .isEmpty()) {

            // Obtain the access code from the qr-read url
            mCurrentAccessCode = qrUri.getFragment();

            final String baseUrl = qrUri.getScheme() + "://" + qrUri.getAuthority();
            // Obtain the service details and set the backend
            SampleApplication.getMfaSdk().getServiceDetails(baseUrl, new MPinMfaAsync.Callback<ServiceDetails>() {

                @Override
                protected void onSuccess(@Nullable ServiceDetails result) {
                    mCurrentServiceDetails = result;
                    SampleApplication.getMfaSdk().setBackend(mCurrentServiceDetails, new MPinMfaAsync.Callback<Void>() {

                        @Override
                        protected void onSuccess(@Nullable Void result) {
                            // If the backend is set successfully, we can retrieve the session details using the access code
                            SampleApplication.getMfaSdk()
                                    .getSessionDetails(mCurrentAccessCode, new MPinMfaAsync.Callback<SessionDetails>() {

                                        @Override
                                        protected void onSuccess(@Nullable SessionDetails result) {
                                            onBackendSet();
                                        }

                                        @Override
                                        protected void onFail(@NonNull Status status) {
                                            // Retrieving session details failed
                                            ToastUtils.showStatus(QrReaderActivity.this, status);

                                            // Resume scanning for qr code
                                            startQrReading();
                                        }
                                    });
                        }

                        @Override
                        protected void onFail(@NonNull Status status) {
                            // The setting of backend failed
                            ToastUtils.showStatus(QrReaderActivity.this, status);

                            // Resume scanning for qr code
                            startQrReading();
                        }
                    });
                }

                @Override
                protected void onFail(final @NonNull Status status) {
                    ToastUtils.showStatus(QrReaderActivity.this, status);

                    // Resume scanning for qr code
                    startQrReading();
                }
            });
        } else {
            // The url does not have the expected format
            Toast.makeText(this, "Invalid qr url", Toast.LENGTH_SHORT).show();
            //Resume scanning for qr code
            startQrReading();
        }
    }

    @Override
    public void onPinEntered(final String pin) {
        // Start the authentication process with the scanned access code and a registered user
        SampleApplication.getMfaSdk().startAuthentication(mCurrentUser, mCurrentAccessCode, new MPinMfaAsync.Callback<Void>() {

            @Override
            protected void onSuccess(@Nullable Void result) {
                SampleApplication.getMfaSdk()
                        .finishAuthentication(mCurrentUser, new String[]{pin}, mCurrentAccessCode, new MPinMfaAsync.Callback<Void>() {

                            @Override
                            protected void onResult(final @NonNull Status status, @Nullable Void result) {
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {

                                        if (status.getStatusCode() == com.miracl.mpinsdk.model.Status.Code.OK) {
                                            // The authentication for the user is successful
                                            Toast.makeText(QrReaderActivity.this,
                                                    "Successfully logged " + mCurrentUser.getId() + " with " + mCurrentUser.getBackend(),
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            // Authentication failed
                                            Toast.makeText(QrReaderActivity.this,
                                                    "Status code: " + status.getStatusCode() + " message: " + status.getErrorMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        }

                                        // Resume scanning for qr code
                                        startQrReading();
                                    }
                                });
                            }
                        });
            }

            @Override
            protected void onFail(@NonNull Status status) {
                ToastUtils.showStatus(QrReaderActivity.this, status);
                startQrReading();
            }
        });
    }

    @Override
    public void onPinCanceled() {
        // Resume scanning for qr code
        startQrReading();
    }

    private void onBackendSet() {
        SampleApplication.setCurrentAccessCode(mCurrentAccessCode);
        SampleApplication.getMfaSdk().doInBackground(new MPinMfaAsync.Callback<MPinMFA>() {

            @Override
            protected void onResult(@NonNull Status status, @Nullable MPinMFA sdk) {
                if (sdk != null) {
                    // Get the list of stored users in order to check if there is
                    // an existing user that can be logged in
                    List<User> users = new ArrayList<>();
                    List<User> registeredUsers = new ArrayList<>();
                    com.miracl.mpinsdk.model.Status listUsersStatus = sdk.listUsers(users);
                    for (User user : users) {
                        if (user.getState() == User.State.REGISTERED) {
                            registeredUsers.add(user);
                        } else {
                            // delete users that are not registered, because the sample does not handle such cases
                            sdk.deleteUser(user);
                        }
                    }

                    if (listUsersStatus.getStatusCode() == com.miracl.mpinsdk.model.Status.Code.OK) {
                        // Filter the registered users for the current backend
                        Uri currentBackend = Uri.parse(mCurrentServiceDetails.backendUrl);
                        if (currentBackend != null && currentBackend.getAuthority() != null) {
                            final List<User> currentBackendRegisteredUsers = new ArrayList<>();
                            for (User user : registeredUsers) {
                                if (user.getBackend().equalsIgnoreCase(currentBackend.getAuthority())) {
                                    currentBackendRegisteredUsers.add(user);
                                }
                            }

                            if (currentBackendRegisteredUsers.isEmpty()) {
                                // If there are no users, we need to register a new one
                                startActivity(new Intent(QrReaderActivity.this, RegisterUserActivity.class));
                            } else {
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        // If there is a registered user start the authentication process
                                        mCurrentUser = currentBackendRegisteredUsers.get(0);
                                        mEnterPinDialog.setTitle(mCurrentUser.getId());
                                        mEnterPinDialog.show();
                                    }
                                });
                            }
                        }
                    } else {
                        ToastUtils.showStatus(QrReaderActivity.this, status);
                    }
                }
            }
        });
    }

    private void startQrReading() {
        QrReaderFragment qrReaderFragment = (QrReaderFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (qrReaderFragment != null) {
            qrReaderFragment.updateQrCodeProcessingStatus(false);
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, QrReaderFragment.newInstance())
                    .commit();
        }
    }
}
