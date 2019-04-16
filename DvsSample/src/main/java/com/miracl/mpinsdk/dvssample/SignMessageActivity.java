package com.miracl.mpinsdk.dvssample;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
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
                    protected void onSuccess(@Nullable Boolean result) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mEnterPinDialog.setTitle(getString(R.string.title_enter_pin_for_signing));
                                mEnterPinDialog.show();
                            }
                        });
                    }

                    @Override
                    protected void onFail(@NonNull Status status) {
                        mMessageDialog.show(status);
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
                    protected void onSuccess(@Nullable Signature result) {
                        verifySignature(documentDvsInfo, result);
                    }

                    @Override
                    protected void onFail(@NonNull Status status) {
                        mMessageDialog.show(status);
                    }
                });
    }

    private void verifySignature(DocumentDvsInfo documentDvsInfo, Signature signature) {
        try {
            // Serialize the signature and document data
            String verificationData = serializeSignature(signature);
            String documentData = serializeDocumentDvsInfo(documentDvsInfo);

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