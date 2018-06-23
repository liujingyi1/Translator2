package com.rgk.android.translator.mpush;

import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.mpush.api.http.HttpCallback;
import com.mpush.api.http.HttpResponse;
import com.mpush.client.ClientConfig;
import com.rgk.android.translator.BuildConfig;
import com.rgk.android.translator.TranslatorApp;
import com.rgk.android.translator.database.beans.MessageBean;
import com.rgk.android.translator.mpush.domain.RxSchedulers;
import com.rgk.android.translator.mpush.domain.ServerApi;
import com.rgk.android.translator.mpush.domain.ServerApiService;
import com.rgk.android.translator.mpush.domain.ServerResponse;
import com.rgk.android.translator.mpush.sdk.MPush;
import com.rgk.android.translator.mpush.sdk.MPushLog;
import com.rgk.android.translator.utils.Utils;

import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MPushApiProxy2 implements IMPushApi {

    private static String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCghPCWCobG8nTD24juwSVataW7iViRxcTkey/B792VZEhuHjQvA3cAJgx2Lv8GnX8NIoShZtoCg3Cx6ecs+VEPD2fBcg2L4JK7xldGpOJ3ONEAyVsLOttXZtNXvyDZRijiErQALMTorcgi79M5uVX9/jMv2Ggb2XAeZhlLD28fHwIDAQAB";
    public static String SERVER_IP = "http://116.62.168.13:9999";

    private String mUserId;
    private String mPairUserId;

    private HttpProxyCallback httpProxyCallback;
    private HttpClientListener httpClientListener;
    private Context mContext;

    ServerApi mServerApi;

    private ServerApi.ServerApiListener serverApiListener = new ServerApi.ServerApiListener() {
        @Override
        public void onError(int errorCode) {

        }
    };

    public MPushApiProxy2(Context context) {
        mContext = context;
        mServerApi = ServerApi.getInstance();
        mServerApi.setServerApiListener(serverApiListener);
    }

    public void setPairUser(String id) {
        mPairUserId = id;
    }

    public String getPairUser() {
        return mPairUserId;
    }

    private String getDeviceId() {
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Activity.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        if (TextUtils.isEmpty(deviceId)) {
            String time = Long.toString((System.currentTimeMillis() / (1000 * 60 * 60)));
            deviceId = time + time;
        }
        return deviceId;
    }

    @Override
    public void bindUser(String userId) {

        if (!TextUtils.isEmpty(userId)) {
            mUserId = userId;
            MPush.I.bindAccount(mUserId, "mpush:" + (int) (Math.random() * 10));
        }
    }

    private void initPush(String allocServer, String userId) {
        //公钥有服务端提供和私钥对应

        MPushLog pushLog = new MPushLog();
        pushLog.enable(true);

        ClientConfig cc = ClientConfig.build()
                .setPublicKey(PUBLIC_KEY)
                .setAllotServer(allocServer)
                .setDeviceId(getDeviceId())
                .setClientVersion(BuildConfig.VERSION_NAME)
                .setLogger(pushLog)
                .setLogEnabled(BuildConfig.DEBUG)
                .setEnableHttpProxy(true)
                .setUserId(userId);
        MPush.I.checkInit(mContext.getApplicationContext()).setClientConfig(cc);
    }

    @Override
    public void startPush(final String userId) {

        mUserId = userId;
        initPush(SERVER_IP, mUserId);

        MPush.I.checkInit(mContext).startPush();
        Toast.makeText(mContext.getApplicationContext(), "start push", Toast.LENGTH_SHORT).show();

        if (!mServerApi.getIsRegisted()) {
            mServerApi.registClient();
        }
    }

    @Override
    public void sendPush(final MessageBean messageBean) {
        if (messageBean == null) return;

        if (MPush.I.hasStarted() && MPush.I.hasRunning()) {

//            final HashMap<String, String> sendParams = new HashMap();
//            sendParams.put("type", "2");
//            sendParams.put("content", messageBean.getText());
//            sendParams.put("from", messageBean.getLanguage());
//            sendParams.put("fromDeviceId", mUserId);
//            sendParams.put("toDeviceId", mPairUserId);
//            sendParams.put("broadcast", "false");




            final SendMessageBean sendMessageBean = SendMessageBean
                    .build()
                    .setType(2)
                    .setContent(messageBean.getText())
                    .setFrom("zh_cn")
                    .setFromDeviceId(mUserId)
                    .setToDeviceId(mPairUserId)
                    .setBroadcast(false);
            Log.i("jingyi", "sendMessageBean="+sendMessageBean.toString());

            ServerApi.apiService(ServerApiService.class)
                    .sendMessage(mServerApi.getToken(), sendMessageBean)
                    .observeOn(Schedulers.io())
                    .flatMap(new Function<ServerResponse, ObservableSource<ServerResponse>>() {
                        @Override
                        public ObservableSource<ServerResponse> apply(ServerResponse serverResponse) throws Exception {
                            Log.i("jingyi", "123123 serverResponse.code=" + serverResponse.code);
                            if (serverResponse.code == 1011) {
                                return ServerApi.apiService(ServerApiService.class)
                                        .getToken(Utils.getDeviceId(TranslatorApp.getAppContext()))
                                        .flatMap(new Function<ServerResponse, ObservableSource<ServerResponse>>() {
                                            @Override
                                            public ObservableSource<ServerResponse> apply(ServerResponse serverResponse) throws Exception {
                                                if (serverResponse.code == 1) {
                                                    mServerApi.setToken(serverResponse.result);

                                                    return ServerApi.apiService(ServerApiService.class)
                                                            .sendMessage(serverResponse.result, sendMessageBean);
                                                }
                                                return Observable.just(serverResponse);
                                            }
                                        });
                            }
                            return Observable.just(serverResponse);
                        }
                    })
                    .compose(RxSchedulers.<ServerResponse>io_main())
                    .subscribe(new Consumer<ServerResponse>() {
                        @Override
                        public void accept(ServerResponse serverResponse) throws Exception {
                            if (serverResponse.code != 1) {
                                processError(serverResponse.code);
                            }
                            Log.i("jingyi", "serverResponse code=" + serverResponse.code);
                            Log.i("jingyi", "serverResponse result=" + serverResponse.result);
                            Log.i("jingyi", "serverResponse message=" + serverResponse.message);

                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Log.i("jingyi", "throwable=" + throwable.getMessage());

                        }
                    });
        }

//            if (httpResponse.statusCode == 200) {
//                String message = new String(httpResponse.body, Constants.UTF_8);
//                ServerResponse response = new Gson().fromJson(message, new TypeToken<ServerResponse>(){}.getType());
//                Log.i("jingyi", "response code="+response.code);
//                Log.i("jingyi", "response message="+response.message);
//                if (response.code == 1011) {
//            MPush.I.sendHttpProxy(request);

    }

    private void processError(int code) {
        
    }

    HttpCallback mHttpCallback = new HttpCallback() {
        @Override
        public void onResponse(HttpResponse httpResponse) {

            Log.i("jingyi", "httpResponse="+httpResponse.toString());

            if (httpProxyCallback != null) {
                httpProxyCallback.onResponse(httpResponse);
            }
        }

        @Override
        public void onCancelled() {
            if (httpProxyCallback != null) {
                httpProxyCallback.onCancelled();
            }
        }
    };

    @Override
    public void stopPush() {
        MPush.I.stopPush();
        Toast.makeText(mContext, "stop push", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void pausePush() {
        MPush.I.pausePush();
        Toast.makeText(mContext, "pause push", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void resumePush() {
        MPush.I.resumePush();
        Toast.makeText(mContext, "resume push", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void unbindUser() {
        MPush.I.unbindAccount();
        Toast.makeText(mContext, "unbind user", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setHttpCallBack(HttpProxyCallback httpCallBack) {
        this.httpProxyCallback = httpCallBack;
    }
    @Override
    public void setHttpClientListener(HttpClientListener httpClientListener) {
        MPush.I.setHttpClientListener(httpClientListener);
    }

//    class HttpResponse {
//        long limitTime;
//        String content;
//    }
}
