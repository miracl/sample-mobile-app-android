package com.miracl.mpinsdk.dvssample;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

class EnterPinDialog extends Dialog {

    private static final int PIN_LENGTH = 4;

    private final EventListener mEventListener;
    private EditText mEnterPinInput;
    private TextView mTitle;

    public EnterPinDialog(Context context, EventListener listener) {
        super(context);
        mEventListener = listener;
        init();
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle.setText(title);
        if (mTitle.getVisibility() != View.VISIBLE) {
            mTitle.setVisibility(View.VISIBLE);
        }
    }

    private void init() {
        setContentView(R.layout.dialog_enter_pin);

        mTitle = findViewById(R.id.enter_pin_title);
        mEnterPinInput = findViewById(R.id.enter_pin_input);
        mEnterPinInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == PIN_LENGTH) {
                    if (mEventListener != null) {
                        mEventListener.onPinEntered(charSequence.toString());
                    }
                    dismiss();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                mEnterPinInput.setText("");
                if (mEventListener != null) {
                    mEventListener.onPinCanceled();
                }
            }
        });
    }

    public interface EventListener {

        void onPinEntered(String pin);

        void onPinCanceled();
    }
}
