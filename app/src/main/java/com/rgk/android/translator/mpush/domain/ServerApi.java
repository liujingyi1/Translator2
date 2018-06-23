package com.rgk.android.translator.mpush.domain;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.rgk.android.translator.TranslatorApp;
import com.rgk.android.translator.utils.Utils;

import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ServerApi {
    private static ServerApi mInstance = null;
    private ServerApiManager mApiManager = null;

    private static final String SP_PUSH_FILE_NAME = "pushapi.config";
    private static final String SP_KEY_TOKEN = "token1";
    private static final String SP_KEY_SERVER_IP = "serverIp";
    private static final String SP_KEY_IS_REGISTED = "registed";
    private static final String SP_KEY_AS = "allotServer";
    private static final String SP_KEY_AT = "account";
    private static final String SP_KEY_TG = "tags";
    private static final String SP_KEY_LG = "log";

    private static final int SUCCESS_CODE = 1;
    String  mToken = null;
    String  mServerIp = null;

    Handler handler;

    ServerApiListener mServerApiListener;
    public void setServerApiListener(ServerApiListener serverApiListener) {
        this.mServerApiListener = serverApiListener;
    }

    public interface GetTokenCallback{
        public void call(int code);
    }

    public interface ServerApiListener {
        public void onError(int errorCode);
    }

    private ServerApi() {
        mApiManager = new ServerApiManager();
    }

    public static <T> T apiService(Class<T> clz) {
        return getInstance().mApiManager.getService(clz);
    }

    public static ServerApi getInstance() {
        if (mInstance == null) {
            mInstance = new ServerApi();
        }
        return mInstance;
    }

    public <T> void addApiService(Class<T> clz) {
        getInstance().mApiManager.addService(clz);
    }

//    public String getToken(final OnGetTokenListener tokenListener, boolean needRefresh) {
//        if (TextUtils.isEmpty(mToken) && !needRefresh) {
    public String getToken() {
        if (TextUtils.isEmpty(mToken)) {
            SharedPreferences sp = TranslatorApp.getAppContext().getSharedPreferences(SP_PUSH_FILE_NAME, Context.MODE_PRIVATE);
            mToken = sp.getString(SP_KEY_TOKEN, "");
        }
        return mToken;
    }

    public void setToken(String token) {
        mToken = token;
        SharedPreferences sp = TranslatorApp.getAppContext().getSharedPreferences(SP_PUSH_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SP_KEY_TOKEN, mToken);
        editor.commit();
    }

    public String getServerIp() {
        if (TextUtils.isEmpty(mServerIp)) {
            SharedPreferences sp = TranslatorApp.getAppContext().getSharedPreferences(SP_PUSH_FILE_NAME, Context.MODE_PRIVATE);
            mServerIp = sp.getString(SP_KEY_SERVER_IP, "");
        }
        return mServerIp;
    }

    public void setServerIp(String ip) {
        mServerIp = ip;

        SharedPreferences sp = TranslatorApp.getAppContext().getSharedPreferences(SP_PUSH_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SP_KEY_SERVER_IP, mServerIp);
        editor.commit();
    }

    public boolean getIsRegisted() {
        SharedPreferences sp = TranslatorApp.getAppContext().getSharedPreferences(SP_PUSH_FILE_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(SP_KEY_IS_REGISTED, false);
    }

    public void setIsRegisted(Boolean is) {
        SharedPreferences sp = TranslatorApp.getAppContext().getSharedPreferences(SP_PUSH_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SP_KEY_IS_REGISTED, is);
        editor.commit();
    }

    private void getTokenObservable(final GetTokenCallback callback) {
        ServerApi.apiService(ServerApiService.class)
                .getToken(Utils.getDeviceId(TranslatorApp.getAppContext()))
                .subscribeOn(Schedulers.io())
                .flatMap(new Function<ServerResponse, ObservableSource<ServerResponse>>() {
                    @Override
                    public ObservableSource<ServerResponse> apply(ServerResponse serverResponse) throws Exception {
                        mToken = serverResponse.result;
                        SharedPreferences sp = TranslatorApp.getAppContext().getSharedPreferences(SP_PUSH_FILE_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString(SP_KEY_TOKEN, mToken);
                        editor.commit();

                        callback.call(serverResponse.code);
                        return null;
                    }
                });
    }

    private Observable<ServerListResponse> getTokenObservable(final Observable<ServerListResponse> observable, int a) {
        return ServerApi.apiService(ServerApiService.class)
                .getToken(Utils.getDeviceId(TranslatorApp.getAppContext()))
                .subscribeOn(Schedulers.io())
                .flatMap(new Function<ServerResponse, ObservableSource<ServerListResponse>>() {
                    @Override
                    public ObservableSource<ServerListResponse> apply(ServerResponse serverResponse) throws Exception {
                        mToken = serverResponse.result;
                        SharedPreferences sp = TranslatorApp.getAppContext().getSharedPreferences(SP_PUSH_FILE_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString(SP_KEY_TOKEN, mToken);
                        editor.commit();
                        return observable;
                    }
                });
    }

    private Observable<ServerListResponse> getServerIpObservable() {
        return ServerApi.apiService(ServerApiService.class)
                .getServerList(getToken())
                .observeOn(Schedulers.io())
                .flatMap(new Function<ServerListResponse, ObservableSource<ServerListResponse>>() {
                    @Override
                    public ObservableSource<ServerListResponse> apply(ServerListResponse serverResponse) throws Exception {
                        if (serverResponse.code == 1011) {
                            return ServerApi.apiService(ServerApiService.class)
                                    .getToken(Utils.getDeviceId(TranslatorApp.getAppContext()))
                                    .flatMap(new Function<ServerResponse, ObservableSource<ServerListResponse>>() {
                                        @Override
                                        public ObservableSource<ServerListResponse> apply(ServerResponse serverResponse) throws Exception {
                                            if (serverResponse.code == 1) {
                                                mToken = serverResponse.result;
                                                setToken(serverResponse.result);

                                                return ServerApi.apiService(ServerApiService.class)
                                                        .getServerList(mToken);
                                            } else {
                                                return null;
                                            }
                                        }
                                    });
                        } else if (serverResponse.code == 1) {
                            int count = serverResponse.result.size();
                            Random random = new Random();
                            int randomIndex = random.nextInt(count);
                            ServerListResponse.ServerIpBean serverIpBean = serverResponse.result.get(randomIndex);
                            mServerIp = serverIpBean.attrs.public_ip+":"+serverIpBean.port;

                            SharedPreferences sp = TranslatorApp.getAppContext().getSharedPreferences(SP_PUSH_FILE_NAME, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString(SP_KEY_SERVER_IP, mServerIp);
                            editor.commit();
                        }
                        return Observable.just(serverResponse);
                    }
                })
                .compose(RxSchedulers.<ServerListResponse>io_main());


//                .subscribe(new Consumer<ServerListResponse>() {
//                    @Override
//                    public void accept(ServerListResponse serverResponse) throws Exception {
//
//                        if (serverResponse.code == 1) {
//                            int count = serverResponse.result.size();
//                            Random random = new Random();
//                            int randomIndex = random.nextInt(count);
//                            mServerIp = serverResponse.result.get(randomIndex).attrs.public_ip;
//
//                            SharedPreferences sp = TranslatorApp.getAppContext().getSharedPreferences(SP_PUSH_FILE_NAME, Context.MODE_PRIVATE);
//                            SharedPreferences.Editor editor = sp.edit();
//                            editor.putString(SP_KEY_SERVER_IP, mServerIp);
//                            editor.apply();
//                        }
//                    }
//                }, new Consumer<Throwable>() {
//                    @Override
//                    public void accept(Throwable throwable) throws Exception {
//                        Log.i("jingyi", "throwable="+throwable.getMessage());
//                    }
//                });
    }

    public void registClient() {
        ServerApi.apiService(ServerApiService.class)
                .registClient(getToken(), Utils.getDeviceId(TranslatorApp.getAppContext()))
                .observeOn(Schedulers.io())
                .flatMap(new Function<ServerResponse, ObservableSource<ServerResponse>>() {
                    @Override
                    public ObservableSource<ServerResponse> apply(ServerResponse serverResponse) throws Exception {
                        if (serverResponse.code == 1011) {
                            return ServerApi.apiService(ServerApiService.class)
                                    .getToken(Utils.getDeviceId(TranslatorApp.getAppContext()))
                                    .subscribeOn(Schedulers.io())
                                    .flatMap(new Function<ServerResponse, ObservableSource<ServerResponse>>() {
                                        @Override
                                        public ObservableSource<ServerResponse> apply(ServerResponse serverResponse) throws Exception {
                                            if (serverResponse.code == 1) {
                                                mToken = serverResponse.result;
                                                setToken(serverResponse.result);

                                                return ServerApi.apiService(ServerApiService.class)
                                                        .registClient(mToken, Utils.getDeviceId(TranslatorApp.getAppContext()));
                                            } else {
                                                return null;
                                            }
                                        }
                                    });
//                            return getTokenObservable(ServerApi.apiService(ServerApiService.class)
//                                    .registClient(getToken(), Utils.getDeviceId(TranslatorApp.getAppContext())));
                        }
                        return Observable.just(serverResponse);
                    }
                })
                .compose(RxSchedulers.<ServerResponse>io_main())
                .subscribe(new Consumer<ServerResponse>() {
                    @Override
                    public void accept(ServerResponse serverResponse) throws Exception {
                        if (serverResponse != null
                                && (serverResponse.code == 1
                                || serverResponse.code == 1008)) {
                            setIsRegisted(true);
                            Log.i("jingyi", "RegistClient Success");
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
    }

    public void pairClient(final String fromDeviceId, final long pairCode) {
        ServerApi.apiService(ServerApiService.class)
                .pairClient(getToken(), fromDeviceId, pairCode)
                .observeOn(Schedulers.io())
                .flatMap(new Function<ServerResponse, ObservableSource<ServerResponse>>() {
                    @Override
                    public ObservableSource<ServerResponse> apply(ServerResponse serverResponse) throws Exception {
                        if (serverResponse.code == 1011) {

                            return ServerApi.apiService(ServerApiService.class)
                                    .getToken(Utils.getDeviceId(TranslatorApp.getAppContext()))
                                    .subscribeOn(Schedulers.io())
                                    .flatMap(new Function<ServerResponse, ObservableSource<ServerResponse>>() {
                                        @Override
                                        public ObservableSource<ServerResponse> apply(ServerResponse serverResponse) throws Exception {
                                            if (serverResponse.code == 1) {
                                                mToken = serverResponse.result;
                                                setToken(serverResponse.result);

                                                return ServerApi.apiService(ServerApiService.class)
                                                        .pairClient(getToken(), fromDeviceId, pairCode);
                                            }
                                            return  Observable.just(serverResponse);
                                        }
                                    });
                        }
                        return Observable.just(serverResponse);
                    }
                })
                .compose(RxSchedulers.<ServerResponse>io_main());
    }

    public void getPairCode(final String fromDeviceId) {
        ServerApi.apiService(ServerApiService.class)
                .getPairCode(getToken(), fromDeviceId)
                .observeOn(Schedulers.io())
                .flatMap(new Function<ServerResponse, ObservableSource<ServerResponse>>() {
                    @Override
                    public ObservableSource<ServerResponse> apply(ServerResponse serverResponse) throws Exception {
                        if (serverResponse.code == 1011) {

                            return ServerApi.apiService(ServerApiService.class)
                                    .getToken(Utils.getDeviceId(TranslatorApp.getAppContext()))
                                    .subscribeOn(Schedulers.io())
                                    .flatMap(new Function<ServerResponse, ObservableSource<ServerResponse>>() {
                                        @Override
                                        public ObservableSource<ServerResponse> apply(ServerResponse serverResponse) throws Exception {
                                            if (serverResponse.code == 1) {
                                                mToken = serverResponse.result;
                                                setToken(serverResponse.result);

                                                return ServerApi.apiService(ServerApiService.class)
                                                        .getPairCode(getToken(), fromDeviceId);
                                            }
                                            return  Observable.just(serverResponse);
                                        }
                                    });
                        }
                        return Observable.just(serverResponse);
                    }
                })
                .compose(RxSchedulers.<ServerResponse>io_main());
    }

    public void invalidPairCode(final String fromDeviceId, final long code) {
        ServerApi.apiService(ServerApiService.class)
                .invalidPairCode(getToken(), fromDeviceId, code)
                .observeOn(Schedulers.io())
                .flatMap(new Function<ServerResponse, ObservableSource<ServerResponse>>() {
                    @Override
                    public ObservableSource<ServerResponse> apply(ServerResponse serverResponse) throws Exception {
                        if (serverResponse.code == 1011) {

                            return ServerApi.apiService(ServerApiService.class)
                                    .getToken(Utils.getDeviceId(TranslatorApp.getAppContext()))
                                    .subscribeOn(Schedulers.io())
                                    .flatMap(new Function<ServerResponse, ObservableSource<ServerResponse>>() {
                                        @Override
                                        public ObservableSource<ServerResponse> apply(ServerResponse serverResponse) throws Exception {
                                            if (serverResponse.code == 1) {
                                                mToken = serverResponse.result;
                                                setToken(serverResponse.result);

                                                return ServerApi.apiService(ServerApiService.class)
                                                        .invalidPairCode(getToken(), fromDeviceId, code);
                                            }
                                            return  Observable.just(serverResponse);
                                        }
                                    });
                        }
                        return Observable.just(serverResponse);
                    }
                })
                .compose(RxSchedulers.<ServerResponse>io_main());
    }

    public void relievePairCode(final String fromDeviceId, final String toDeviceId) {
        ServerApi.apiService(ServerApiService.class)
                .relievePair(getToken(), fromDeviceId, toDeviceId)
                .observeOn(Schedulers.io())
                .flatMap(new Function<ServerResponse, ObservableSource<ServerResponse>>() {
                    @Override
                    public ObservableSource<ServerResponse> apply(ServerResponse serverResponse) throws Exception {
                        if (serverResponse.code == 1011) {

                            return ServerApi.apiService(ServerApiService.class)
                                    .getToken(Utils.getDeviceId(TranslatorApp.getAppContext()))
                                    .subscribeOn(Schedulers.io())
                                    .flatMap(new Function<ServerResponse, ObservableSource<ServerResponse>>() {
                                        @Override
                                        public ObservableSource<ServerResponse> apply(ServerResponse serverResponse) throws Exception {
                                            if (serverResponse.code == 1) {
                                                mToken = serverResponse.result;
                                                setToken(serverResponse.result);

                                                return ServerApi.apiService(ServerApiService.class)
                                                        .relievePair(getToken(), fromDeviceId, toDeviceId);
                                            }
                                            return  Observable.just(serverResponse);
                                        }
                                    });
                        }
                        return Observable.just(serverResponse);
                    }
                })
                .compose(RxSchedulers.<ServerResponse>io_main());
    }

    public void getPairInfo(final String deviceId) {
        ServerApi.apiService(ServerApiService.class)
                .getPairInfo(getToken(), deviceId)
                .observeOn(Schedulers.io())
                .flatMap(new Function<ServerResponse, ObservableSource<ServerResponse>>() {
                    @Override
                    public ObservableSource<ServerResponse> apply(ServerResponse serverResponse) throws Exception {
                        if (serverResponse.code == 1011) {

                            return ServerApi.apiService(ServerApiService.class)
                                    .getToken(Utils.getDeviceId(TranslatorApp.getAppContext()))
                                    .subscribeOn(Schedulers.io())
                                    .flatMap(new Function<ServerResponse, ObservableSource<ServerResponse>>() {
                                        @Override
                                        public ObservableSource<ServerResponse> apply(ServerResponse serverResponse) throws Exception {
                                            if (serverResponse.code == 1) {
                                                mToken = serverResponse.result;
                                                setToken(serverResponse.result);

                                                return ServerApi.apiService(ServerApiService.class)
                                                        .getPairInfo(getToken(), deviceId);
                                            }
                                            return  Observable.just(serverResponse);
                                        }
                                    });
                        }
                        return Observable.just(serverResponse);
                    }
                })
                .compose(RxSchedulers.<ServerResponse>io_main());
    }

    public void getPairInfo(final String fromeDeviceInfo, final String toDeviceId) {
        ServerApi.apiService(ServerApiService.class)
                .hangUp(getToken(),fromeDeviceInfo,toDeviceId)
                .observeOn(Schedulers.io())
                .flatMap(new Function<ServerResponse, ObservableSource<ServerResponse>>() {
                    @Override
                    public ObservableSource<ServerResponse> apply(ServerResponse serverResponse) throws Exception {
                        if (serverResponse.code == 1011) {

                            return ServerApi.apiService(ServerApiService.class)
                                    .getToken(Utils.getDeviceId(TranslatorApp.getAppContext()))
                                    .subscribeOn(Schedulers.io())
                                    .flatMap(new Function<ServerResponse, ObservableSource<ServerResponse>>() {
                                        @Override
                                        public ObservableSource<ServerResponse> apply(ServerResponse serverResponse) throws Exception {
                                            if (serverResponse.code == 1) {
                                                mToken = serverResponse.result;
                                                setToken(serverResponse.result);

                                                return ServerApi.apiService(ServerApiService.class)
                                                        .hangUp(getToken(),fromeDeviceInfo,toDeviceId);
                                            }
                                            return  Observable.just(serverResponse);
                                        }
                                    });
                        }
                        return Observable.just(serverResponse);
                    }
                })
                .compose(RxSchedulers.<ServerResponse>io_main());
    }

}
