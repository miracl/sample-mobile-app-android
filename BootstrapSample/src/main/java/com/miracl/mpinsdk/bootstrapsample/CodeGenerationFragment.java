package com.miracl.mpinsdk.bootstrapsample;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.miracl.mpinsdk.MPinMFA;
import com.miracl.mpinsdk.MPinMfaAsync;
import com.miracl.mpinsdk.model.RegCode;
import com.miracl.mpinsdk.model.Status;
import com.miracl.mpinsdk.model.User;

import java.util.ArrayList;
import java.util.List;

public class CodeGenerationFragment extends Fragment implements EnterPinDialog.EventListener {

    private static final String ARG_USER_ID = "user-id";

    private OnCodeGenerationFragmentInteractionListener  mListener;
    private User mCurrentUser;
    private MessageDialog mMessageDialog;
    private EnterPinDialog mEnterPinDialog;

    public CodeGenerationFragment() {
        // Required empty public constructor
    }

    public static CodeGenerationFragment newInstance(String userId) {
        CodeGenerationFragment fragment = new CodeGenerationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMessageDialog = new MessageDialog(getContext());
        mEnterPinDialog = new EnterPinDialog(getContext(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_code_generation, container, false);

        String userId = null;
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
        }

        if (userId != null) {
            getCurrentUser(userId);
        } else {
            mCurrentUser = null;
        }
        initView(view);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCodeGenerationFragmentInteractionListener ) {
            mListener = (OnCodeGenerationFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onPinEntered(String pin) {
        startAuthenticationRegCode(pin);
    }

    @Override
    public void onPinCanceled() {

    }

    private void getCurrentUser(final String userId) {
        MPinMFA sdk = SampleApplication.getMfaSdk().getMfaSdk();
        if (sdk != null) {
            // Get the list of stored users
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
                String backendUrl = getString(R.string.mpin_backend);
                Uri currentBackend = Uri.parse(backendUrl);

                if (currentBackend != null && currentBackend.getAuthority() != null) {
                    for (User user : registeredUsers) {
                        if (user.getBackend().equalsIgnoreCase(currentBackend.getAuthority()) && user.getId().equals(userId)) {
                            mCurrentUser = user;
                            return;
                        }
                    }
                }
            } else {
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // Listing user for the current backend failed
                        mMessageDialog.show(listUsersStatus);
                    }
                });
            }
        }
    }
    private void startAuthenticationRegCode(final String pin) {
        //  Start an authentication process in order to obtain a bootstrap code
        SampleApplication.getMfaSdk().startAuthenticationRegCode(mCurrentUser, new MPinMfaAsync.Callback<Void>() {

            @Override
            protected void onSuccess(@Nullable Void result) {
                finishAuthenticationRegCode(pin);
            }

            @Override
            protected void onFail(final @NonNull Status status) {
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // Authentication failed
                        mMessageDialog.show(status);
                    }
                });
            }
        });
    }

    private void finishAuthenticationRegCode(String pin) {
        // Finalize the authentication process
        SampleApplication.getMfaSdk().finishAuthenticationRegCode(mCurrentUser, new String[]{pin}, new MPinMfaAsync.Callback<RegCode>() {
            @Override
            protected void onSuccess(@Nullable final RegCode regCode) {
                // If the authentication for the bootstrap code is successful, an RegCode object is passed
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getView().findViewById(R.id.bootstrap_code_label).setVisibility(View.VISIBLE);
                        ((TextView) getView().findViewById(R.id.tv_bootstrap_code)).setText(regCode.otp);
                    }
                });

            }

            @Override
            protected void onFail(final @NonNull Status status) {
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // Authentication failed
                        mMessageDialog.show(status);
                    }
                });
            }
        });
    }

    private void onBootstrapClick() {
        mEnterPinDialog.setTitle(mCurrentUser.getId());
        mEnterPinDialog.show();
    }

    private void initView(View view) {
        view.findViewById(R.id.select_user_button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mListener.onCodeGenerationFragmentInteraction();
            }
        });
        view.findViewById(R.id.register_user_button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), RegisterUserActivity.class));
            }
        });
        view.findViewById(R.id.button_generate_code).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBootstrapClick();
            }
        });

        if (mCurrentUser != null) {
            ((TextView) view.findViewById(R.id.user_id)).setText(mCurrentUser.getId());
            ((TextView) view.findViewById(R.id.user_backend)).setText(mCurrentUser.getBackend());
            ((TextView) view.findViewById(R.id.user_state)).setText(mCurrentUser.getState().toString());
            ((TextView) view.findViewById(R.id.user_cid)).setText(mCurrentUser.getCustomerId());
            View mUserInfo = view.findViewById(R.id.user_info);
            mUserInfo.setVisibility(View.VISIBLE);
            view.findViewById(R.id.action_buttons).setVisibility(View.GONE);
        }
    }

    public interface OnCodeGenerationFragmentInteractionListener  {
        void onCodeGenerationFragmentInteraction();
    }
}
