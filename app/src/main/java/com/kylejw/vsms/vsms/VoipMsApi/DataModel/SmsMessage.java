package com.kylejw.vsms.vsms.VoipMsApi.DataModel;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by Kyle on 12/13/2014.
 */
public class SmsMessage {
    private long id;
    private long date;
    private MessageType type;
    private String did;
    private String contact;
    private String message;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public enum MessageType {
        SENT,
        RECEIVED
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public MessageType getMessageType()
    {
        return type;
    }

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private SmsMessage(){}

    public  SmsMessage(long id, long date, MessageType type, String from, String to, String message) {
        setId(id);
        setDate(date);
        setType(type);
        setDid(from);
        setContact(to);
        setMessage(message);
    }

    public static SmsMessage parseJson(JSONObject jo) {
        SmsMessage msg = new SmsMessage();

        if (jo.has("contact")) {
            try {
                msg.setContact(jo.getString("contact"));
            } catch (JSONException ex) {
            }
        }

        if (jo.has("date")) {
            try {
                Calendar c = Calendar.getInstance();
                c.setTime(DATE_FORMAT.parse(jo.getString("date")));

                msg.setDate(c.getTimeInMillis());
            } catch (Exception ex) {
                msg.setDate(-1);
            }
        }

        if (jo.has("did")) {
            try {
                msg.setDid(jo.getString("did"));
            } catch (JSONException ex) {
            }
        }

        if (jo.has("id")) {
            try {
                msg.setId(Long.parseLong(jo.getString("id")));
            } catch (JSONException ex) {
            }
        }

        if (jo.has("message")) {
            try {
                msg.setMessage(jo.getString("message"));
            } catch (JSONException ex) {
            }
        }

        if (jo.has("type")) {
            try {
                String type = jo.getString("type");
                switch(type) {
                    case "0":
                        msg.type = MessageType.SENT;
                        break;
                    case "1":
                        msg.type = MessageType.RECEIVED;
                        break;
                    default:
                        msg.type = null;
                        break;
                }
            } catch (Exception ex) {}

        }

        return msg;
    }

    @Override
    public String toString(){

        if (getMessageType() == MessageType.SENT) {
            return "SENT:  " + message;
        } else {
            return "RECD:  " + message;
        }
    }

}