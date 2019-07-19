package com.miracl.mpinsdk.bootstrapsample;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.miracl.mpinsdk.MPinMfaAsync;
import com.miracl.mpinsdk.model.Status;
import com.miracl.mpinsdk.model.User;

public class RegisterUserActivity extends AppCompatActivity implements View.OnClickListener, EnterPinDialog.EventListener {

    private EditText mEmailInput;
    private Button mSubmitButton;
    private View mConfirmControls;
    private Button mConfirmButton;
    private Button mResendButton;
    private EnterPinDialog mEnterPinDialog;
    private MessageDialog mMessageDialog;

    private User mCurrentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        mEnterPinDialog = new EnterPinDialog(RegisterUserActivity.this, RegisterUserActivity.this);
        mMessageDialog = new MessageDialog(this);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetViews();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mEnterPinDialog.dismiss();
        // In order not to clutter the sample app with users, remove the current user if it is not registered on
        // navigating away from the app
        if (mCurrentUser != null && mCurrentUser.getState() != User.State.REGISTERED) {
            SampleApplication.getMfaSdk().deleteUser(mCurrentUser, null);
        }
    }

    @Override
    public void onPinEntered(final String pin) {
        if (mCurrentUser == null) {
            return;
        }

        // Once we have the user's pin we can finish the registration process
        SampleApplication.getMfaSdk().finishRegistration(mCurrentUser, new String[]{pin}, new MPinMfaAsync.Callback<Void>() {

            @Override
            protected void onSuccess(@Nullable Void result) {
                // The registration for the user is complete
                Intent intent = new Intent(RegisterUserActivity.this, MainActivity.class);
                intent.putExtra(MainActivity.EXTRA_USER_ID, mCurrentUser.getId());
                startActivity(intent);
                finish();
            }

            @Override
            protected void onFail(final @NonNull Status status) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // Finishing registration has failed
                        mMessageDialog.show(status);
                        enableControls();
                    }
                });
            }
        });
    }

    @Override
    public void onPinCanceled() {
        enableControls();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register_submit_button:
                onSubmitClick();
                return;
            case R.id.register_confirm_button:
                onConfirmClick();
                return;
            case R.id.register_resend_button:
                onResendClick();
                return;
            default:
                break;
        }
    }

    private void onConfirmClick() {
        if (mCurrentUser == null) {
            return;
        }

        disableControls();
        // After the user has followed the steps in the verification mail, it must be confirmed from the SDK
        // in order to proceed with the registration process
        SampleApplication.getMfaSdk().confirmRegistration(mCurrentUser, new MPinMfaAsync.Callback<Void>() {

            @Override
            protected void onSuccess(@Nullable Void result) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mEnterPinDialog.show();
                    }
                });
            }

            @Override
            protected void onFail(final @NonNull Status status) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // Confirmation has failed
                        mMessageDialog.show(status);
                        enableControls();
                    }
                });
            }
        });
    }

    private void onResendClick() {
        if (mCurrentUser == null) {
            return;
        }

        disableControls();
        // If for some reason we need to resend the verification mail, the registration process for the user must be
        // restarted
        SampleApplication.getMfaSdk().restartRegistration(mCurrentUser, new MPinMfaAsync.Callback<Void>() {

            @Override
            protected void onSuccess(@Nullable Void result) {
                // If restarting the registration process is successful a new verification mail is sent
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        enableControls();
                        mMessageDialog.show("Email has been sent to " + mCurrentUser.getId());
                    }
                });
            }

            @Override
            protected void onFail(final @NonNull Status status) {
                // Restarting registration has failed
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mMessageDialog.show(status);
                        enableControls();
                    }
                });
            }
        });
    }

    private void onSubmitClick() {
        final String email = mEmailInput.getText().toString().trim();
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mMessageDialog.show("Invalid email address entered");
            return;
        }

        disableControls();
        String access_code_service_base_url = getString(R.string.access_code_service_base_url);
        new AccessCodeObtainingTask(access_code_service_base_url, new AccessCodeObtainingTask.Callback() {

            @Override
            public void onSuccess() {
                onStartedRegistration(email);
            }

            @Override
            public void onFail(Status status) {
                mMessageDialog.show(status);
                enableControls();
            }
        }).execute();
    }

    private void onStartedRegistration(final String email) {
        // Obtain a user object from the SDK. The id of the user is an email and while it is not mandatory to provide
        // device name note that some backends may require it
        SampleApplication.getMfaSdk().makeNewUser(email, "Android Sample App", new MPinMfaAsync.Callback<User>() {

            @Override
            protected void onSuccess(User result) {
                mCurrentUser = result;
                // After we have a user, we can start the registration process for it. If successful this will trigger sending a
                // confirmation email from the current backend
                SampleApplication.getMfaSdk()
                        .startRegistration(SampleApplication.getCurrentAccessCode(), mCurrentUser, new MPinMfaAsync.Callback<Void>() {

                            @Override
                            protected void onSuccess(@Nullable Void result) {
                                // When the registration process is started successfully for a user, the identity is
                                // stored in the SDK, associating it with the current set backend.
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        mSubmitButton.setVisibility(View.GONE);
                                        mConfirmControls.setVisibility(View.VISIBLE);
                                        mMessageDialog.show("Email has been sent to " + mCurrentUser.getId());
                                        enableControls();
                                    }
                                });
                            }

                            @Override
                            protected void onFail(final @NonNull Status status) {
                                // Starting registration has failed
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        mMessageDialog.show(status);
                                        enableControls();
                                    }
                                });
                            }
                        });
            }

            @Override
            protected void onFail(final @NonNull Status status) {
                // Starting registration has failed
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mMessageDialog.show(status);
                        enableControls();
                    }
                });
            }
        });
    }

    private void onEmailChanged(CharSequence textInput) {
        if (mSubmitButton.getVisibility() != View.VISIBLE) {
            disableControls();

            // If the email is changed after the registration is started, we delete the identity (because it will get
            // stored otherwise) and effectively restart the registration process. This is solely not to clutter the
            // sample app with users
            SampleApplication.getMfaSdk().deleteUser(mCurrentUser, new MPinMfaAsync.Callback<Void>() {

                @Override
                protected void onResult(@NonNull Status status, @Nullable Void result) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            mCurrentUser = null;
                            mConfirmControls.setVisibility(View.GONE);
                            mSubmitButton.setVisibility(View.VISIBLE);
                            enableControls();
                        }
                    });
                }
            });
        } else {
            if (textInput.length() == 0 && mSubmitButton.isEnabled()) {
                mSubmitButton.setEnabled(false);
            } else {
                mSubmitButton.setEnabled(true);
            }
        }
    }

    private void disableControls() {
        mSubmitButton.setEnabled(false);
        mConfirmButton.setEnabled(false);
        mResendButton.setEnabled(false);
        mEmailInput.setEnabled(false);
    }

    private void enableControls() {
        mSubmitButton.setEnabled(true);
        mConfirmButton.setEnabled(true);
        mResendButton.setEnabled(true);
        mEmailInput.setEnabled(true);
    }

    private void initViews() {
        mEmailInput = findViewById(R.id.register_email_input);
        mConfirmControls = findViewById(R.id.register_confirm_controls);

        mSubmitButton = findViewById(R.id.register_submit_button);
        mSubmitButton.setOnClickListener(this);
        mConfirmButton = findViewById(R.id.register_confirm_button);
        mConfirmButton.setOnClickListener(this);
        mResendButton = findViewById(R.id.register_resend_button);
        mResendButton.setOnClickListener(this);

        mEmailInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                onEmailChanged(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void resetViews() {
        mEmailInput.setText("");
        mSubmitButton.setEnabled(false);
        mSubmitButton.setVisibility(View.VISIBLE);
        mConfirmControls.setVisibility(View.GONE);
    }
}
