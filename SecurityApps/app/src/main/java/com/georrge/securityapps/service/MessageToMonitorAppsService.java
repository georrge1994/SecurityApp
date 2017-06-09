package com.georrge.securityapps.service;


/**
 * Created by Георгий on 24.01.2017.
 */
public class MessageToMonitorAppsService {

    public final String message;        // transmit message
    public final int code;              // type of message

    public MessageToMonitorAppsService(int code, String message) {
        this.code = code; // 0 - stop service, 1 - password security app, 2 - allowed app
        this.message = message;
    }
}
