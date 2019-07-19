package com.miracl.mpinsdk.bootstrapsample;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.miracl.mpinsdk.MPinMfaAsync;
import com.miracl.mpinsdk.model.Status;
import com.miracl.mpinsdk.model.User;

public class CodeRegistrationFragment extends Fragment implements EnterPinDialog.EventListener {

    private EditText mEmailInput;
    private EditText mBootstrapCodeInput;
    private Button mSubmitButton;
    private EnterPinDialog mEnterPinDialog;
    private MessageDialog mMessageDialog;

    private User mCurrentUser;

    private boolean isEmailValid = false;
    private boolean isBootstrapCodeValid = false;

    public CodeRegistrationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEnterPinDialog = new EnterPinDialog(getActivity(), CodeRegistrationFragment.this);
        mMessageDialog = new MessageDialog(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_code_registration, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        resetViews();
    }

    @Override
    public void onStop() {
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
        SampleApplication.getMfaSdk().confirmRegistration(mCurrentUser, new MPinMfaAsync.Callback<Void>() {

            @Override
            protected void onSuccess(@Nullable Void result) {
                SampleApplication.getMfaSdk().finishRegistration(mCurrentUser, new String[]{pin}, new MPinMfaAsync.Callback<Void>() {

                    @Override
                    protected void onSuccess(@Nullable Void result) {
                        // The registration for the user is complete
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mMessageDialog.show(getString(R.string.registration_successful_message));
                                resetViews();
                            }
                        });
                    }

                    @Override
                    protected void onFail(final @NonNull Status status) {
                        getActivity().runOnUiThread(new Runnable() {

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
            protected void onFail(final @NonNull Status status) {
                getActivity().runOnUiThread(new Runnable() {

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

    @Override
    public void onPinCanceled() {
        enableControls();
    }

    private void onSubmitClick() {
        final String email = mEmailInput.getText().toString().trim();
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mMessageDialog.show("Invalid email address entered");
            return;
        }

        final String bootstrapCode = mBootstrapCodeInput.getText().toString().trim();
        if (bootstrapCode.length() != 6) {
            mMessageDialog.show("Bootstrap code should be 6 digits long");
            return;
        }

        disableControls();
        String access_code_service_base_url = getString(R.string.access_code_service_base_url);
        new AccessCodeObtainingTask(access_code_service_base_url, new AccessCodeObtainingTask.Callback() {

            @Override
            public void onSuccess() {
                onStartedRegistration(email, bootstrapCode);
            }

            @Override
            public void onFail(Status status) {
                mMessageDialog.show(status);
                enableControls();
            }
        }).execute();
    }

    private void onStartedRegistration(final String email, final String bootstrapCode) {
        SampleApplication.getMfaSdk().isUserExisting(email, new MPinMfaAsync.Callback<Boolean>() {
            @Override
            protected void onSuccess(@Nullable Boolean isUserExisting) {
                if (isUserExisting) {
                    mMessageDialog.show(getString(R.string.user_already_registered_message));
                    enableControls();
                    return;
                } else {
                    // Obtain a user object from the SDK. The id of the user is an email and while it is not mandatory to provide
                    // device name note that some backends may require it
                    SampleApplication.getMfaSdk().makeNewUser(email, "Android Sample App", new MPinMfaAsync.Callback<User>() {

                        @Override
                        protected void onSuccess(User result) {
                            mCurrentUser = result;
                            // After we have a user, we can start the registration process for it.
                            SampleApplication.getMfaSdk()
                                    .startRegistration(SampleApplication.getCurrentAccessCode(), mCurrentUser, null, bootstrapCode, new MPinMfaAsync.Callback<Void>() {

                                        @Override
                                        protected void onSuccess(@Nullable Void result) {
                                            // When the registration process is started successfully for a user, the identity is
                                            // stored in the SDK, associating it with the current set backend.
                                            getActivity().runOnUiThread(new Runnable() {

                                                @Override
                                                public void run() {
                                                    mEnterPinDialog.show();
                                                }
                                            });
                                        }

                                        @Override
                                        protected void onFail(final @NonNull Status status) {
                                            // Starting registration has failed
                                            getActivity().runOnUiThread(new Runnable() {

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
                            getActivity().runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    mMessageDialog.show(status);
                                    enableControls();
                                }
                            });
                        }
                    });
                }
            }

            @Override
            protected void onFail(final @NonNull Status status) {
                getActivity().runOnUiThread(new Runnable() {

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
        if (textInput.length() == 0 && mSubmitButton.isEnabled()) {
            isEmailValid = false;
        } else {
            isEmailValid = true;
        }

        mSubmitButton.setEnabled(isEmailValid && isBootstrapCodeValid);
    }

    private void onBootstrapCodeChanged(CharSequence textInput) {
        if (textInput.length() != 6) {
            isBootstrapCodeValid = false;
        } else {
            isBootstrapCodeValid = true;
        }

        mSubmitButton.setEnabled(isEmailValid && isBootstrapCodeValid);
    }

    private void disableControls() {
        mSubmitButton.setEnabled(false);
        mEmailInput.setEnabled(false);
        mBootstrapCodeInput.setEnabled(false);
    }

    private void enableControls() {
        mSubmitButton.setEnabled(true);
        mEmailInput.setEnabled(true);
        mBootstrapCodeInput.setEnabled(true);
    }

    private void initViews(View view) {
        mEmailInput = view.findViewById(R.id.register_email_input);
        mBootstrapCodeInput = view.findViewById(R.id.bootstrap_code_input);
        mSubmitButton = view.findViewById(R.id.register_submit_button);

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

        mBootstrapCodeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                onBootstrapCodeChanged(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSubmitClick();
            }
        });
    }

    private void resetViews() {
        mEmailInput.setText("");
        mBootstrapCodeInput.setText("");
        mSubmitButton.setEnabled(false);
        mSubmitButton.setVisibility(View.VISIBLE);
    }
}
