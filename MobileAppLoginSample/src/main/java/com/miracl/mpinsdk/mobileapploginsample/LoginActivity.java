package com.miracl.mpinsdk.mobileapploginsample;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.miracl.mpinsdk.MPinMFA;
import com.miracl.mpinsdk.MPinMfaAsync;
import com.miracl.mpinsdk.model.Status;
import com.miracl.mpinsdk.model.User;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements EnterPinDialog.EventListener {

    private User           mCurrentUser;
    private EnterPinDialog mEnterPinDialog;
    private View           mUserInfo;
    private MessageDialog  mMessageDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEnterPinDialog = new EnterPinDialog(this, this);
        mMessageDialog = new MessageDialog(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        configureSdkAndCurrentUser();
    }


    @Override
    public void onPinEntered(final String pin) {
        final String accessCode = SampleApplication.getCurrentAccessCode();
        if (mCurrentUser != null && accessCode != null) {
            SampleApplication.getMfaSdk().startAuthentication(mCurrentUser, accessCode, new MPinMfaAsync.Callback<Void>() {

                @Override
                protected void onSuccess(@Nullable Void result) {
                    SampleApplication.getMfaSdk().finishAuthenticationAuthCode(mCurrentUser, new String[]{pin}, accessCode,
                      new MPinMfaAsync.Callback<String>() {

                          @Override
                          protected void onResult(final @NonNull Status status, final @Nullable String authCode) {
                              runOnUiThread(new Runnable() {

                                  @Override
                                  public void run() {
                                      if (mCurrentUser != null && mCurrentUser.getState() == User.State.BLOCKED) {
                                          // If the user's identity gets blocked, we delete it
                                          new AlertDialog.Builder(LoginActivity.this).setMessage(
                                            "Identity has been blocked because of too many wrong PIN entries. You will need to create it again.")
                                            .setPositiveButton("OK", null)
                                            .setOnDismissListener(new DialogInterface.OnDismissListener() {

                                                @Override
                                                public void onDismiss(DialogInterface dialogInterface) {
                                                    onDeleteClick();
                                                }
                                            }).show();
                                          return;
                                      }

                                      if (status.getStatusCode() == com.miracl.mpinsdk.model.Status.Code.OK && authCode != null) {
                                          // The authentication for the user is successful
                                          validateLogin(authCode);
                                      } else {
                                          // Authentication failed
                                          mMessageDialog.show(status);
                                      }
                                  }
                              });
                          }
                      });
                }

                @Override
                protected void onFail(final @NonNull Status status) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // Authentication failed
                            mMessageDialog.show(status);
                        }
                    });
                }
            });
        } else {
            // We don't have a current user or an access code
            mMessageDialog.show("Can't login right now, try again");
        }
    }

    private void validateLogin(String authCode) {
        // We use the auth code from the sdk to validate the login with a demo service
        final String clientService = getString(R.string.access_code_service_base_url);
        if (!clientService.isEmpty() && mCurrentUser != null && authCode != null) {
            new ValidateLoginTask(clientService, authCode, mCurrentUser.getId(), new ValidateLoginTask.ValidationListener() {

                @Override
                public void onValidate(boolean isSuccessful) {
                    if (isSuccessful) {
                        mMessageDialog.show(
                          "Successfully logged " + mCurrentUser.getId() + " to " + clientService + " with " + mCurrentUser
                            .getBackend());
                    } else {
                        mMessageDialog
                          .show("Failed to validate login to " + clientService + " with " + mCurrentUser.getBackend());
                    }
                }
            }).execute();
        } else {
            mMessageDialog.show("Failed to validate login");
        }
    }

    @Override
    public void onPinCanceled() {

    }

    private void configureSdkAndCurrentUser() {
        SampleApplication.getMfaSdk().doInBackground(new MPinMfaAsync.Callback<MPinMFA>() {

            @Override
            protected void onResult(@NonNull Status status, @Nullable MPinMFA sdk) {
                if (sdk != null) {
                    // Set the cid and the backend with which the SDK will be configured
                    sdk.setCid(getString(R.string.mpin_cid));
                    Status setBackendStatus = sdk.setBackend(getString(R.string.mpin_backend));
                    if (setBackendStatus.getStatusCode() == com.miracl.mpinsdk.model.Status.Code.OK) {
                        // If the backend and CID are set successfully we can check for a registered user
                        getCurrentUserAndInit();
                    } else {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this,
                                  "The MPin SDK did not initialize properly. Check your backend and CID configuration",
                                  Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        });
    }

    private void getCurrentUserAndInit() {
        SampleApplication.getMfaSdk().doInBackground(new MPinMfaAsync.Callback<MPinMFA>() {

            @Override
            protected void onResult(@NonNull final Status status, @Nullable MPinMFA sdk) {
                if (sdk != null) {
                    // Get the list of stored users in order to check if there is
                    // an existing user that can be logged in with
                    List<User> users = new ArrayList<>();
                    List<User> registeredUsers = new ArrayList<>();
                    final com.miracl.mpinsdk.model.Status listUsersStatus = sdk.listUsers(users);
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
                        String backendUrl = getString(R.string.mpin_backend);
                        Uri currentBackend = Uri.parse(backendUrl);

                        if (currentBackend != null && currentBackend.getAuthority() != null) {
                            final List<User> currentBackendRegisteredUsers = new ArrayList<>();
                            for (User user : registeredUsers) {
                                if (user.getBackend().equalsIgnoreCase(currentBackend.getAuthority())) {
                                    currentBackendRegisteredUsers.add(user);
                                }
                            }

                            if (currentBackendRegisteredUsers.isEmpty()) {
                                // If there are no users, we need to register a new one
                                startActivity(new Intent(LoginActivity.this, RegisterUserActivity.class));
                                finish();
                            } else {
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        // If there is a registered user show info
                                        mCurrentUser = currentBackendRegisteredUsers.get(0);
                                        initView();
                                    }
                                });
                            }
                        }
                    } else {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                // Listing user for the current backend failed
                                mMessageDialog.show(listUsersStatus);
                            }
                        });
                    }
                }
            }
        });
    }

    private void startLogin() {
        mEnterPinDialog.setTitle(mCurrentUser.getId());
        mEnterPinDialog.show();
    }

    private void onDeleteClick() {
        if (mCurrentUser != null) {
            mUserInfo.setVisibility(View.GONE);
            SampleApplication.getMfaSdk().deleteUser(mCurrentUser, new MPinMfaAsync.Callback<Void>() {

                @Override
                protected void onResult(@NonNull Status status, @Nullable Void result) {
                    // After we delete the current user, start the registration flow again
                    startActivity(new Intent(LoginActivity.this, RegisterUserActivity.class));
                    finish();
                }
            });
        }
    }

    private void onLoginClick() {
        if (mCurrentUser != null) {
            new AccessCodeObtainingTask(getString(R.string.access_code_service_base_url), new AccessCodeObtainingTask.Callback() {

                @Override
                public void onSuccess() {
                    startLogin();
                }

                @Override
                public void onFail(Status status) {
                    mMessageDialog.show(status);
                }
            }).execute();
        }
    }

    private void initView() {
        if (mCurrentUser != null) {
            ((TextView) findViewById(R.id.user_id)).setText(mCurrentUser.getId());
            ((TextView) findViewById(R.id.user_backend)).setText(mCurrentUser.getBackend());
            ((TextView) findViewById(R.id.user_state)).setText(mCurrentUser.getState().toString());
            ((TextView) findViewById(R.id.user_cid)).setText(mCurrentUser.getCustomerId());
            findViewById(R.id.delete_user_button).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    onDeleteClick();
                }
            });
            findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    onLoginClick();
                }
            });
            mUserInfo = findViewById(R.id.user_info);
            mUserInfo.setVisibility(View.VISIBLE);
        }
    }
}
