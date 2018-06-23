package com.rgk.android.translator.settings.pair;

import android.app.Fragment;
import android.content.Context;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.mpush.api.Client;
import com.mpush.api.http.HttpResponse;
import com.rgk.android.translator.R;
import com.rgk.android.translator.database.beans.MessageBean;
import com.rgk.android.translator.mpush.HttpClientListener;
import com.rgk.android.translator.mpush.HttpProxyCallback;
import com.rgk.android.translator.mpush.IMPushApi;
import com.rgk.android.translator.mpush.MPushApi;
import com.rgk.android.translator.storage.TStorageManager;
import com.rgk.android.translator.utils.Utils;
import com.rgk.android.translator.view.NumberBoardView;
import com.rgk.android.translator.view.PairCodeInput;


import java.util.Random;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class PairSettingFragment extends Fragment {
    @BindView(R.id.user_id)
    TextView userIdView;
    @BindView(R.id.pair_id)
//    EditText pairIdView;
            PairCodeInput pairIdView;
    @BindView(R.id.info)
    TextView infoView;
    @BindView(R.id.my_device_id)
    TextView myDeviceId;
    @BindView(R.id.pair_device_id)
    TextView pairDeviceId;
    @BindView(R.id.pairing_group)
    ViewGroup pairingGroup;
    @BindView(R.id.paired_group)
    ViewGroup pairedGroup;
    @BindView(R.id.pair_btn)
    Button pairBtn;
    @BindView(R.id.code_time)
    TextView codeTimeView;
    KeyboardView keyboardView;

    String mUserId;
    String mDeviceId;
    String mPairDeviceId;
    String mPairUserId;
    IMPushApi mPushApi;
    Boolean isPairOK = false;
    private static int SECOND = 100;

    private Unbinder unbinder;

    View mRootView;
    private static String PAIR_PREFERENCE = "PAIR_INFO_PREFERENCE";
    Disposable mPairDisposable;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDeviceId = Utils.getDeviceId(getContext());

        mPushApi = MPushApi.get(getContext());

        String deviceId = TStorageManager.getInstance().getPairedId();

        isPairOK = !TextUtils.isEmpty(mPushApi.getPairUser());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.activity_pair, container, false);

        unbinder = ButterKnife.bind(this, mRootView);
        keyboardView = (KeyboardView)mRootView.findViewById(R.id.keyboard_view);
        initViews();

        showView();

        return mRootView;
    }

    @Override
    public void onResume() {
        mPushApi.resumePush();
        super.onResume();
    }

    private void initViews() {

        NumberBoardView boardView = new NumberBoardView(getActivity(), keyboardView, pairIdView.getEditText());
        boardView.showKeyboard();
//        NumberBoardView.shared(PairActivity.this, keyboardView, pairIdView.getEditText()).showKeyboard();

        RxView.clicks(pairBtn)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object serverResponse) throws Exception {
                        String pairUserId = pairIdView.getInputContent();
                        mPushApi.setPairUser(pairUserId);

                        MessageBean sendMessageBean = new MessageBean(1);
                        sendMessageBean.setText("RequestId DeviceId:"+mDeviceId+","+mUserId);
                        mPushApi.sendPush(sendMessageBean);
                    }
                });

        pairIdView.setInputCompleteListener(new PairCodeInput.InputCompleteListener() {
            @Override
            public void inputComplete() {
//                onClick();
            }

            @Override
            public void deleteContent() {

            }
        });
    }

    private void showView() {
        if (!isPairOK) {
            pairedGroup.setVisibility(View.GONE);
            pairingGroup.setVisibility(View.VISIBLE);
            startPairing();
        } else {
            pairedGroup.setVisibility(View.VISIBLE);
            pairingGroup.setVisibility(View.GONE);
            showPaired();
        }
    }

    private void showPaired() {
        myDeviceId.setText(mDeviceId);
        pairDeviceId.setText(mPushApi.getPairUser());
    }

    private void startPairing() {
        mUserId = getRandomCode();
        userIdView.setText(mUserId);
        pairIdView.clearInputContent();

        Observable.interval(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .take(SECOND)
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mPairDisposable = d;
                    }

                    @Override
                    public void onNext(Long aLong) {
                        if (isPairOK) {
                            try {
                                RxTextView.text(codeTimeView).accept("剩余" + (SECOND - aLong) + "秒");
                                mPairDisposable.dispose();
                            } catch (Exception e) {

                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("jingyi", e.toString());
                    }

                    @Override
                    public void onComplete() {
                        try {
                            mUserId = null;
                            RxTextView.text(codeTimeView).accept ("重新获取配对码");
                        } catch (Exception e) {

                        }
                    }
                });


        mPushApi.startPush(mUserId);
        mPushApi.bindUser(mUserId);

        mPushApi.setHttpCallBack(new HttpProxyCallback() {
            @Override
            public void onResponse(HttpResponse httpResponse) {
                if(httpResponse.reasonPhrase.compareTo("OK") != 0) {
                    Toast.makeText(getContext(), "配对失败", Toast.LENGTH_LONG);
                    showView();
                }
            }

            @Override
            public void onCancelled() {
                Toast.makeText(getContext(),"发送失败", Toast.LENGTH_LONG);
            }
        });
        mPushApi.setHttpClientListener(new HttpClientListener(){
            @Override
            public void onReceivePush(Client client, MessageBean messageBean, int i) {
                super.onReceivePush(client, messageBean, i);

                String content = messageBean.getText();

                Log.i("jingyi", "PairActivity deviceId="+mPairDeviceId);
                Log.i("jingyi", "PairActivity content="+messageBean.getText());

                if (content.indexOf("RequestId") != -1){

                    String[] idlist = content.split("DeviceId:")[1].split(",");
                    mPairDeviceId = idlist[0];
                    mPairUserId = idlist[1];

                    Log.i("jingyi", "receive RequestId mPairDeviceId:"+mPairDeviceId+" mPairUserId="+mPairUserId);

                    MessageBean sendMessageBean = new MessageBean(1);
                    sendMessageBean.setText("DeviceId:"+mDeviceId+","+mUserId);
                    mPushApi.setPairUser(mPairUserId);
                    mPushApi.sendPush(sendMessageBean);

                } else if (content.indexOf("DeviceId:") != -1) {
                    String[] idlist = content.split("DeviceId:")[1].split(",");
                    mPairDeviceId = idlist[0];
                    mPairUserId = idlist[1];

                    mPushApi.setPairUser(mPairUserId);
                    MessageBean sendMessageBean = new MessageBean(1);
                    sendMessageBean.setText("Pair OK");
                    mPushApi.sendPush(sendMessageBean);

                    Log.i("jingyi", "receive mPairDeviceId="+mPairDeviceId+" mPairUserId="+mPairUserId);
                } else if (content.indexOf("Pair OK") != -1){
                    if (!isPairOK) {
                        isPairOK = true;
                        MessageBean sendMessageBean = new MessageBean(1);
                        sendMessageBean.setText("Pair OK");
                        mPushApi.sendPush(sendMessageBean);

                        mPushApi.bindUser(mDeviceId);
                        mPushApi.setPairUser(mPairDeviceId);

                        TStorageManager.getInstance().setPairedId(mPairDeviceId);

                        H.sendEmptyMessageDelayed(MSG_SHOW_VIEW, 200);
                    }
                }
            }
        });
    }

    private static final int MSG_SHOW_VIEW = 1;
    Handler H = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SHOW_VIEW: {
                    showView();
                }
            }
        }
    };

    private String getRandomCode() {

        StringBuilder codeStr = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 4; i++) {
            codeStr.append(random.nextInt(9));
        }
        return codeStr.toString();
    }

    @OnClick(R.id.unpair_btn)
    public void onClickUnpair() {

        TStorageManager.getInstance().setPairedId(mPairDeviceId);

        mPushApi.setPairUser(null);
        isPairOK = false;
        showView();
    }

    @Override
    public void onPause() {
        mPushApi.pausePush();
        super.onPause();
    }

    @Override
    public void onDestroy() {

        if (!isPairOK) {
            mPushApi.setPairUser(null);
        }

        mPairDisposable.dispose();
        unbinder.unbind();

        super.onDestroy();
    }


}
