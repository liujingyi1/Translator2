package com.rgk.android.translator.mpush.domain;

public class ServerResponse {
    public int code;
    public String result;
    public String message;

    class RegInfo {
        public String deviceId;
        public String groupId;
        public String id;
        public String shortNum;
    }
    class AddChannelInfo {
        public String deviceId;
        public String id;
        public String number;
    }

}
