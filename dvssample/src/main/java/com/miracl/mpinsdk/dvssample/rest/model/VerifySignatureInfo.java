package com.miracl.mpinsdk.dvssample.rest.model;

import com.google.gson.annotations.SerializedName;

public class VerifySignatureInfo {
    @SerializedName("verified")
    private boolean verified;

    @SerializedName("status")
    private String status;

    public VerifySignatureInfo(boolean verified, String status) {
        this.verified = verified;
        this.status = status;
    }

    public boolean getVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
