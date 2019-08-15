package com.miracl.mpinsdk.dvssample;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.miracl.mpinsdk.MPinMfaAsync;
import com.miracl.mpinsdk.dvssample.rest.model.DocumentDvsInfo;
import com.miracl.mpinsdk.dvssample.rest.model.VerifySignatureInfo;
import com.miracl.mpinsdk.model.Signature;
import com.miracl.mpinsdk.model.Status;
import com.miracl.mpinsdk.model.User;
import com.miracl.mpinsdk.util.Hex;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;

public class SignMessageActivity extends AppCompatActivity implements EnterPinDialog.EventListener {

    private EnterPinDialog mEnterPinDialog;
    private MessageDialog mMessageDialog;
    private View mSignatureInfoView;
    private View mVerificationResultView;
    private TextView mHashTextView;
    private TextView mUTextView;
    private TextView mVTextView;
    private TextView mMpinIdTextView;
    private TextView mPublicKeyTextView;
    private TextView mDtasTextView;
    private TextView mVerifiedTextView;
    private TextView mStatusTextView;

    private DocumentDvsInfo mDocumentDvsInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_message);

        mEnterPinDialog = new EnterPinDialog(this, this);
        mMessageDialog = new MessageDialog(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    @Override
    public void onPinEntered(String pin) {
        signDocument(mDocumentDvsInfo, pin);
    }

    @Override
    public void onPinCanceled() {

    }

    private void createDocumentHash(final String document) {
        // Send post request to the service in order to create document hash
        new CreateDocumentHashTask(getString(R.string.access_code_service_base_url),
                document,
                new CreateDocumentHashTask.Callback() {

                    @Override
                    public void onSuccess(DocumentDvsInfo documentDvsInfo) {
                        mDocumentDvsInfo = documentDvsInfo;
                        verifyDocumentHash(document, documentDvsInfo);
                    }

                    @Override
                    public void onFail(Status status) {
                        mMessageDialog.show(status);
                    }
                }).execute();
    }

    private void verifyDocumentHash(String document, final DocumentDvsInfo documentDvsInfo) {
        SampleApplication.getMfaSdk().verifyDocumentHash(document.getBytes(),
                documentDvsInfo.getHash().getBytes(),
                new MPinMfaAsync.Callback<Boolean>() {
                    @Override
                    protected void onSuccess(final Boolean result) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (result) {
                                    mEnterPinDialog.setTitle(getString(R.string.title_enter_pin_for_signing));
                                    mEnterPinDialog.show();
                                } else {
                                    String errorMessage = "Failed to verify document hash received from the backend.";
                                    mMessageDialog.show(errorMessage);
                                }
                            }
                        });
                    }

                    @Override
                    protected void onFail(final @NonNull Status status) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mMessageDialog.show(status);
                            }
                        });
                    }
                });
    }

    private void signDocument(final DocumentDvsInfo documentDvsInfo, String pin) {
        // Sign the document hash
        SampleApplication.getMfaSdk().sign(SampleApplication.getLoggedUser(),
                documentDvsInfo.getHash().getBytes(),
                new String[]{pin},
                documentDvsInfo.getTimestamp().intValue(),
                new MPinMfaAsync.Callback<Signature>() {

                    @Override
                    protected void onSuccess(final Signature result) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                displaySignature(result);
                            }
                        });
                        verifySignature(documentDvsInfo, result);
                    }

                    @Override
                    protected void onFail(final @NonNull Status status) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mMessageDialog.show(status);
                            }
                        });
                    }
                });
    }

    private void verifySignature(DocumentDvsInfo documentDvsInfo, final Signature signature) {
        try {
            // Serialize the signature and document data
            final String verificationData = serializeSignature(signature);
            final String documentData = serializeDocumentDvsInfo(documentDvsInfo);

            // Send post request to the service in order to verify document signature
            new VerifySignatureTask(getString(R.string.access_code_service_base_url),
                    verificationData,
                    documentData,
                    new VerifySignatureTask.Callback() {

                        @Override
                        public void onSuccess(VerifySignatureInfo verifySignatureInfo) {
                            String message = String.format("Message verified: %b. Status: %s",
                                    verifySignatureInfo.getVerified(),
                                    verifySignatureInfo.getStatus());

                            mMessageDialog.show(message);
                            mVerificationResultView.setVisibility(View.VISIBLE);
                            mVerifiedTextView.setText(String.valueOf(verifySignatureInfo.getVerified()));
                            mStatusTextView.setText(verifySignatureInfo.getStatus());
                        }

                        @Override
                        public void onFail(Status status) {
                            mMessageDialog.show(status);
                        }
                    }).execute();
        } catch (JSONException e) {
            String errorMessage = "Failed to serialize the signature and the document data.";
            mMessageDialog.show(errorMessage);
        }
    }

    private void init() {
        User user = SampleApplication.getLoggedUser();
        if (user == null) {
            startActivity(new Intent(SignMessageActivity.this, LoginActivity.class));
        } else if (!user.canSign()) {
            startActivity(new Intent(SignMessageActivity.this, DvsRegistrationActivity.class));
        } else {
            initViews();
        }
    }

    private void onSignClick() {
        String message = ((EditText) findViewById(R.id.sign_message_input)).getText().toString();
        createDocumentHash(message);
    }

    private void initViews() {
        String dvsRegisteredIdentityLabel = getString(R.string.label_dvs_registered_identity,
                SampleApplication.getLoggedUser().getId());

        ((TextView) findViewById(R.id.dvs_registered_identity_text))
                .setText(boldEmailInText(dvsRegisteredIdentityLabel));

        findViewById(R.id.sign_message_button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                onSignClick();
            }
        });

        mSignatureInfoView = findViewById(R.id.signature_info);
        mVerificationResultView = findViewById(R.id.verification_result);
        mHashTextView = findViewById(R.id.hash);
        mUTextView = findViewById(R.id.u);
        mVTextView = findViewById(R.id.v);
        mMpinIdTextView = findViewById(R.id.mpin_id);
        mPublicKeyTextView = findViewById(R.id.public_key);
        mDtasTextView = findViewById(R.id.dtas);
        mVerifiedTextView = findViewById(R.id.verified);
        mStatusTextView = findViewById(R.id.status);
    }

    private void displaySignature(Signature signature) {
        mSignatureInfoView.setVisibility(View.VISIBLE);
        mHashTextView.setText(Hex.encode(signature.hash));
        mUTextView.setText(Hex.encode(signature.u));
        mVTextView.setText(Hex.encode(signature.v));
        mMpinIdTextView.setText(new String(signature.mpinId));
        mPublicKeyTextView.setText(Hex.encode(signature.publicKey));
        mDtasTextView.setText(new String(signature.dtas));
    }

    private String serializeDocumentDvsInfo(DocumentDvsInfo documentDvsInfo) throws JSONException {
        JSONObject documentDvsInfoObject = new JSONObject();
        documentDvsInfoObject.put("authToken", documentDvsInfo.getAuthToken());
        documentDvsInfoObject.put("hash", documentDvsInfo.getHash());
        documentDvsInfoObject.put("timestamp", String.valueOf(documentDvsInfo.getTimestamp()));

        return documentDvsInfoObject.toString();
    }

    private String serializeSignature(Signature signature) throws JSONException {
        JSONObject signatureObject = new JSONObject();
        signatureObject.put("dtas", new String(signature.dtas));
        signatureObject.put("mpinId", new String(signature.mpinId));
        signatureObject.put("hash", Hex.encode(signature.hash));
        signatureObject.put("publicKey", Hex.encode(signature.publicKey));
        signatureObject.put("u", Hex.encode(signature.u));
        signatureObject.put("v", Hex.encode(signature.v));

        return signatureObject.toString();
    }

    private SpannableStringBuilder boldEmailInText(String text) {
        final Matcher matcher = android.util.Patterns.EMAIL_ADDRESS.matcher(text);

        final SpannableStringBuilder spannable = new SpannableStringBuilder(text);
        final StyleSpan span = new android.text.style.StyleSpan(android.graphics.Typeface.BOLD);
        while (matcher.find()) {
            spannable.setSpan(
                    span, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        return spannable;
    }
}
