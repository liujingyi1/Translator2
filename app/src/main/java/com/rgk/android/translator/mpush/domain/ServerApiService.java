package com.rgk.android.translator.mpush.domain;

import com.rgk.android.translator.mpush.SendMessageBean;

import java.util.HashMap;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface ServerApiService {

    @Headers("Content-Type:application/json; charset=utf-8")
    @POST("mpush/push/")
    Observable<ServerResponse> sendMessage(
            @Header("token") String token,
            @Body SendMessageBean sendParams
    );

    @GET("client/token/" + "{deviceId}")
    Observable<ServerResponse> getToken(
            @Path("deviceId") String deviceId
    );

    @GET("client/registe/" + "{deviceId}")
    Observable<ServerResponse> registClient(
            @Header("token") String token,
            @Path("deviceId") String deviceId
    );

    @GET("client/pairingCode/get/" + "{deviceId}")
    Observable<ServerResponse> getPairCode(
            @Header("token") String token,
            @Path("deviceId") String deviceId
    );

    @GET("client/pairingCode/invalid/" + "{deviceId}"+"{pairingCode}")
    Observable<ServerResponse> invalidPairCode(
            @Header("token") String token,
            @Path("deviceId") String deviceId,
            @Path("pairingCode") long pairingCode
    );

    @GET("client/paired/" + "{fromDeviceId}"+"{pairingCode}")
    Observable<ServerResponse> pairClient(
            @Header("token") String token,
            @Path("fromDeviceId") String fromDeviceId,
            @Path("pairingCode") long pairingCode
    );

    @GET("client/relieve/" + "{fromDeviceId}"+"{toDeviceId}")
    Observable<ServerResponse> relievePair(
            @Header("token") String token,
            @Path("fromDeviceId") String fromDeviceId,
            @Path("toDeviceId") String toDeviceId
    );

    @GET("channel/getNum")
    Observable<ServerResponse> getChannelNum(
            @Header("token") String token
    );

    @GET("channel/join/" + "{deviceId}"+"{number}")
    Observable<ServerResponse> joinChannel(
            @Header("token") String token,
            @Path("deviceId") String deviceId,
            @Path("number") long number
    );

    @GET("channel/exit/" + "{deviceId}"+"{number}")
    Observable<ServerResponse> exitChannel(
            @Header("token") String token,
            @Path("deviceId") String deviceId,
            @Path("number") long number
    );

    @GET("mpush/serverList")
    Observable<ServerListResponse> getServerList(
            @Header("token") String token
    );

    @GET("mpush/kick"+"{deviceId}")
    Observable<ServerResponse> kickUser(
            @Header("token") String token,
            @Path("deviceId") String deviceId
    );

    @GET("client/paireInfo"+"{deviceId}")
    Observable<ServerResponse> getPairInfo(
            @Header("token") String token,
            @Path("deviceId") String deviceId
    );

    @GET("client/hangUp"+"{fromDeviceId}"+"{toDeviceId}")
    Observable<ServerResponse> hangUp(
            @Header("token") String token,
            @Path("fromDeviceId") String fromDeviceId,
            @Path("toDeviceId") String toDeviceId
    );
}
