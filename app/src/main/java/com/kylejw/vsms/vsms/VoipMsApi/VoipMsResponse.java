package com.kylejw.vsms.vsms.VoipMsApi;

/**
 * Created by Kyle on 12/18/2014.
 */
public class VoipMsResponse {
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSuccess() {
        return this.status.equals("success");
    }

    private String status;
}
