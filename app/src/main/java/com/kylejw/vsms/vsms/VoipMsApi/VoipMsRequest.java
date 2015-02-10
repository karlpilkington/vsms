package com.kylejw.vsms.vsms.VoipMsApi;


import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.kylejw.vsms.vsms.HttpGetter;

import org.json.JSONObject;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.net.URI;
import java.security.InvalidParameterException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kyle on 11/8/2014.
 */
public abstract class VoipMsRequest<TResult extends VoipMsResponse> {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface VoipMsParameter {
        String name() default "";
        boolean required() default false;
    }

    public interface VoipMsCallback<TResult extends  VoipMsResponse> {
        void onComplete(TResult result);
    }

    private boolean authenticate = false;
    private String method = null;
    final private static String RootUrl = "https://voip.ms/api/v1/rest.php?";
    private static String User = "username";
    private static String Pass = "password";

    public VoipMsRequest(String method, boolean authenticate) {
        this.method = method;
        this.authenticate = authenticate;
    }

    private HashMap<String,String> getParameters() {

        HashMap<String,String> params = new HashMap<String, String>();

        for(Field field : this.getClass().getDeclaredFields()) {
            VoipMsParameter p = field.getAnnotation(VoipMsParameter.class);
            if (null == p) continue;

            String name = p.name();
            if (null == name || name.isEmpty()) name = field.getName();

            String value = null;
            try {
                field.setAccessible(true);
                Object valueObject = field.get(this);

                if (null == valueObject) continue;

                value = valueObject.toString();
            } catch (IllegalAccessException ex)
            {
                String kyle = ex.toString();

            }

            if (null == value) {
                if (p.required()) {
                    throw new InvalidParameterException("Missing required parameter " + name);
                } else {
                    continue;
                }
            }

            params.put(name, Uri.encode(value));
        }

        return params;
    }

    private String buildRequestUri() {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(RootUrl);

        urlBuilder.append("method=" + method + "&");

        if (authenticate) {
            urlBuilder.append("api_username=" + User + "&");
            urlBuilder.append("api_password=" + Pass + "&");
        }

        for (Map.Entry<String, String> entry : getParameters().entrySet()) {
            urlBuilder.append(entry.getKey() + "=" + entry.getValue() + "&");
        }

        if (urlBuilder.lastIndexOf("&") == urlBuilder.length() - 1) {
            urlBuilder.deleteCharAt((urlBuilder.length() - 1));
        }

        return  urlBuilder.toString();
    }

    protected abstract TResult buildResult(JSONObject jsonResponse);

    public void executeAsync(final VoipMsCallback<TResult> callback) {

        HttpGetter getter = new HttpGetter() {
            @Override
            public void onComplete(String respStr) {
                JSONObject jsonObject = null;

                try {
                    jsonObject = new JSONObject(respStr);
                } catch (Throwable t) {
                    Log.e("Voip.ms Request", "Could not parse JSON: " + respStr);
                }
                if (null != callback) callback.onComplete(buildResult(jsonObject));
            }
        };


        getter.execute(URI.create(buildRequestUri()));
    }
}
