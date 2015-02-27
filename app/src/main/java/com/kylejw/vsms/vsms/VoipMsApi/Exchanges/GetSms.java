package com.kylejw.vsms.vsms.VoipMsApi.Exchanges;

import com.kylejw.vsms.vsms.VoipMsApi.DataModel.SmsMessage;
import com.kylejw.vsms.vsms.VoipMsApi.VoipMsRequest;
import com.kylejw.vsms.vsms.VoipMsApi.VoipMsResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kyle on 12/13/2014.
 */
public class GetSms extends VoipMsRequest<GetSms.GetSmsResponse> {

    private static final int MAX_AGE_DAYS = 90;

    public class GetSmsResponse extends VoipMsResponse {

        private ArrayList<SmsMessage> sms;

        public ArrayList<SmsMessage> getSms() {
            return sms;
        }

        public void setSms(ArrayList<SmsMessage> sms) {
            this.sms = sms;
        }
    }

    @Override
    protected GetSmsResponse buildResult(JSONObject jsonResponse) {
        GetSmsResponse resp = new GetSmsResponse();

        try {
            resp.setStatus(jsonResponse.getString("status"));
        } catch(JSONException ex) { }

        try {
            JSONArray smsArray = jsonResponse.getJSONArray("sms");

            ArrayList<SmsMessage> messages = new ArrayList<>();

            for (int i = 0; i < smsArray.length(); i++)
            {
                SmsMessage msg = SmsMessage.parseJson(smsArray.getJSONObject(i));
                messages.add(msg);
            }

            resp.setSms(messages);

        } catch (JSONException ex) {}

        return resp;
    }

    @VoipMsParameter(name = "from")
    String from = null;

    @VoipMsParameter
    String to = null;

    @VoipMsParameter
    String type = null;

    @VoipMsParameter
    String did = null;

    @VoipMsParameter
    String contact = null;

    @VoipMsParameter
    String limit = "1000000";

    @VoipMsParameter
    String timezone;

    public GetSms(Calendar from, Calendar to) {
        super("getSMS", true);

        if (null == from) {
            // Never been updated
            from = Calendar.getInstance();
            from.add(Calendar.DATE, -MAX_AGE_DAYS);
        } else {
            // Have a previous update
            long delta = to.getTimeInMillis() - from.getTimeInMillis();
            long dayDelta = TimeUnit.DAYS.convert(delta, TimeUnit.MILLISECONDS);

            // If it's been more than 90 days, cap to 90 days
            if (dayDelta >= MAX_AGE_DAYS) {
                from = Calendar.getInstance();
                from.add(Calendar.DATE, -MAX_AGE_DAYS);
            }
        }

        this.from = String.format(Locale.CANADA, "%s-%s-%s", from.get(Calendar.YEAR), from.get(Calendar.MONTH) + 1,from.get(Calendar.DAY_OF_MONTH));
        this.to = String.format(Locale.CANADA, "%s-%s-%s", to.get(Calendar.YEAR), to.get(Calendar.MONTH) + 1,to.get(Calendar.DAY_OF_MONTH));

        double offset = (double)from.getTimeZone().getRawOffset();
        // Msec to hours
        offset /= 3600000.0;

        this.timezone = Double.toString(offset);
    }

}
