package com.kylejw.vsms.vsms.VoipMsApi.Exchanges;

import android.content.Context;

import com.kylejw.vsms.vsms.VoipMsApi.VoipMsRequest;
import com.kylejw.vsms.vsms.VoipMsApi.VoipMsResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Created by Kyle on 12/18/2014.
 */
public class SendSms extends VoipMsRequest<SendSms.SendSmsResponse> {

    public class SendSmsResponse extends VoipMsResponse {

        private String id;

        public String getId() {
            return id;
        }

        public void setId(String sms) {
            this.id = sms;
        }
    }

    @VoipMsParameter(name = "did", required = true)
    String fromDid;

    @VoipMsParameter(name = "dst", required = true)
    String toDid;

    @VoipMsParameter(name = "message", required = true)
    private String message;

    public void setMessage(String message) {
        if (message.length() > 160) {
            // TODO: Split and send two messages!
            message = message.substring(0,159);
        }

        this.message = message;
    }

    public SendSms(String fromDid, String toDid, String message) {
        super("sendSMS", true);

        this.fromDid = fromDid;
        this.toDid = toDid;
        setMessage(message);
    }

    @Override
    protected SendSmsResponse buildResult(JSONObject jsonResponse) {
        SendSmsResponse resp = new SendSmsResponse();

        try {
            resp.setStatus(jsonResponse.getString("status"));
        } catch (JSONException ex) {}

        try {
            resp.setId(jsonResponse.getString("sms"));
        } catch (JSONException ex) {}

        return resp;
    }

}
