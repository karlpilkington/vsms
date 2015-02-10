package com.kylejw.vsms.vsms.VoipMsApi;

/**
 * Created by Kyle on 1/12/2015.
 */
public abstract class StatusCallback<TResult> {
    public abstract void callback(boolean status, String message, TResult result);
}
