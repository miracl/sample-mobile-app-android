package com.miracl.mpinsdk.bootstrapsample;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.miracl.mpinsdk.MPinMFA;
import com.miracl.mpinsdk.MPinMfaAsync;
import com.miracl.mpinsdk.model.Status;

public class MainActivity extends AppCompatActivity
        implements UsersFragment.OnUsersFragmentInteractionListener,
        CodeGenerationFragment.OnCodeGenerationFragmentInteractionListener {

    public static final String EXTRA_USER_ID = "com.miracl.mpinsdk.bootstrapsample.USER_ID";

    private BottomNavigationView mBottomNavigationView;
    private String mUserId;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_code_generation:
                    selectedFragment = CodeGenerationFragment.newInstance(mUserId);
                    mUserId = null;
                    break;
                case R.id.navigation_code_registration:
                    selectedFragment = new CodeRegistrationFragment();
                    break;
                case R.id.navigation_users:
                    selectedFragment = new UsersFragment();
                    break;
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    selectedFragment).commit();

            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBottomNavigationView = findViewById(R.id.nav_view);
        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        configureSdkAndInit();
    }

    @Override
    public void onCodeGenerationFragmentInteraction() {
        mBottomNavigationView.setSelectedItemId(R.id.navigation_users);
    }

    @Override
    public void onUsersFragmentInteraction(String userId) {
        mUserId = userId;
        mBottomNavigationView.setSelectedItemId(R.id.navigation_code_generation);
    }

    private void configureSdkAndInit() {
        SampleApplication.getMfaSdk().doInBackground(new MPinMfaAsync.Callback<MPinMFA>() {

            @Override
            protected void onResult(@NonNull Status status, @Nullable MPinMFA sdk) {
                if (sdk != null) {
                    // Set the cid and the backend with which the SDK will be configured
                    sdk.setCid(getString(R.string.mpin_cid));
                    Status setBackendStatus = sdk.setBackend(getString(R.string.mpin_backend));
                    if (setBackendStatus.getStatusCode() != com.miracl.mpinsdk.model.Status.Code.OK) {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,
                                        "The MPin SDK did not initialize properly. Check your backend and CID configuration",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        initView();
                    }
                }
            }
        });
    }

    private void initView() {
        Intent intent = getIntent();
        mUserId  = intent.getStringExtra(EXTRA_USER_ID);
        mBottomNavigationView.setSelectedItemId(R.id.navigation_code_generation);
    }
}
