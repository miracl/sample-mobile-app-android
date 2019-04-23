package com.miracl.mpinsdk.dvssample.rest.model;

import com.google.gson.annotations.SerializedName;

public class VerifySignatureInfo {
    @SerializedName("verified")
    private final boolean verified;

    @SerializedName("status")
    private final String status;

    public VerifySignatureInfo(boolean verified, String status) {
        this.verified = verified;
        this.status = status;
    }

    public boolean getVerified() {
        return verified;
    }

    public String getStatus() {
        return status;
    }
}
