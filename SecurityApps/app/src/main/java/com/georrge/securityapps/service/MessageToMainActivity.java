package com.georrge.securityapps.service;

/**
 * Created by Георгий on 24.01.2017.
 */
public class MessageToMainActivity {

    public final String message;        // transmit message
    public final int code;              // type of message
    public final String additional;     //

    public MessageToMainActivity(int code, String message) {
        this.code = code;   // 0 - add new app in SecurityList,
                            // 1 - package name of violator app,
                            // 2 - delete app from Sec.List
        this.message = message;
        this.additional = "";
    }

    public MessageToMainActivity(int code, String message, String additional) {
        this.code = code;   // 0 - add new app in SecurityList,
                            // 1 - package name of violator app,
                            // 2 - delete app from Sec.List
        this.message = message;
        this.additional = additional;
    }
}