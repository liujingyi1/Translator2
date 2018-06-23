package com.rgk.android.translator.mpush.domain;

import java.util.List;

public class ServerListResponse {

    public int code;
    public String message;
    public List<ServerIpBean> result;

    public class ServerIpBean {
        public PublicIp attrs;
        public String host;
        public String port;
        public class PublicIp {
            public String public_ip;
        }
    }
}
