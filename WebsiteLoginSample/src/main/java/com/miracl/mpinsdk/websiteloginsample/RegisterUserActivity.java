package com.miracl.mpinsdk.websiteloginsample;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.miracl.mpinsdk.MPinMfaAsync;
import com.miracl.mpinsdk.model.Status;
import com.miracl.mpinsdk.model.User;

import static android.view.View.GONE;

public class RegisterUserActivity extends AppCompatActivity implements View.OnClickListener, EnterPinDialog.EventListener {

    private EditText       mEmailInput;
    private Button         mSubmitButton;
    private View           mConfirmControls;
    private Button         mConfirmButton;
    private Button         mResendButton;
    private EnterPinDialog mEnterPinDialog;

    private User mCurrentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        mEnterPinDialog = new EnterPinDialog(RegisterUserActivity.this, RegisterUserActivity.this);

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
                startActivity(new Intent(RegisterUserActivity.this, RegistrationSuccessfulActivity.class));
                finish();
            }

            @Override
            protected void onFail(@NonNull Status status) {
                // Finishing registration has failed
                ToastUtils.showStatus(RegisterUserActivity.this, status);
                enableControls();
            }
        });
    }

    @Override
    public void onPinCanceled() {
        enableControls();
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
            protected void onFail(@NonNull Status status) {
                // Confirmation has failed
                ToastUtils.showStatus(RegisterUserActivity.this, status);
                enableControls();
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
                enableControls();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(RegisterUserActivity.this, "Email has been sent to " + mCurrentUser.getId(),
                          Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            protected void onFail(@NonNull Status status) {
                // Restarting registration has failed
                ToastUtils.showStatus(RegisterUserActivity.this, status);
                enableControls();
            }
        });
    }

    private void onSubmitClick() {
        final String email = mEmailInput.getText().toString().trim();
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address entered", Toast.LENGTH_SHORT).show();
            return;
        }

        disableControls();
        // Obtain a user object from the SDK. The id of the user is an email and while it is not mandatory to provide
        // device name note that some backends may require it
        SampleApplication.getMfaSdk().makeNewUser(email, "Android Sample App", new MPinMfaAsync.Callback<User>() {

            @Override
            protected void onSuccess(@Nullable User result) {
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
                                  mSubmitButton.setVisibility(GONE);
                                  mConfirmControls.setVisibility(View.VISIBLE);
                                  Toast.makeText(RegisterUserActivity.this, "Email has been sent to " + mCurrentUser.getId(),
                                    Toast.LENGTH_SHORT).show();
                                  enableControls();
                              }
                          });
                      }

                      @Override
                      protected void onFail(@NonNull Status status) {
                          ToastUtils.showStatus(RegisterUserActivity.this, status);
                          enableControls();
                      }
                  });
            }

            @Override
            protected void onFail(@NonNull Status status) {
                ToastUtils.showStatus(RegisterUserActivity.this, status);
                enableControls();
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

    private void disableControls() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mSubmitButton.setEnabled(false);
                mConfirmButton.setEnabled(false);
                mResendButton.setEnabled(false);
                mEmailInput.setEnabled(false);
            }
        });
    }

    private void enableControls() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mSubmitButton.setEnabled(true);
                mConfirmButton.setEnabled(true);
                mResendButton.setEnabled(true);
                mEmailInput.setEnabled(true);
            }
        });
    }

    private void initViews() {
        mEmailInput = (EditText) findViewById(R.id.register_email_input);
        mConfirmControls = findViewById(R.id.register_confirm_controls);

        mSubmitButton = (Button) findViewById(R.id.register_submit_button);
        mSubmitButton.setOnClickListener(this);
        mConfirmButton = (Button) findViewById(R.id.register_confirm_button);
        mConfirmButton.setOnClickListener(this);
        mResendButton = (Button) findViewById(R.id.register_resend_button);
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
        mConfirmControls.setVisibility(GONE);
    }
}
