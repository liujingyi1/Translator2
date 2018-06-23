package com.rgk.android.translator.composemessage;

import android.app.Activity;

import com.mpush.api.Client;
import com.mpush.api.http.HttpResponse;
import com.rgk.android.translator.AppContext;
import com.rgk.android.translator.database.DbConstants.MessageType;
import com.rgk.android.translator.database.TranslatorStorage;
import com.rgk.android.translator.database.beans.MessageBean;
import com.rgk.android.translator.database.beans.UserBean;
import com.rgk.android.translator.media.MediaManager;
import com.rgk.android.translator.mpush.HttpClientListener;
import com.rgk.android.translator.mpush.HttpProxyCallback;
import com.rgk.android.translator.mpush.IMPushApi;
import com.rgk.android.translator.mpush.MPushApi;
import com.rgk.android.translator.storage.TStorageManager;
import com.rgk.android.translator.stt.ISTT;
import com.rgk.android.translator.stt.ISTTFinishedListener;
import com.rgk.android.translator.stt.ISTTVoiceLevelListener;
import com.rgk.android.translator.stt.STTFactory;
import com.rgk.android.translator.translate.ITranslate;
import com.rgk.android.translator.translate.ITranslateFinishedListener;
import com.rgk.android.translator.tts.ITTS;
import com.rgk.android.translator.tts.ITTSListener;
import com.rgk.android.translator.tts.TTSManager;
import com.rgk.android.translator.utils.Logger;
import com.rgk.android.translator.utils.Utils;
import com.rgk.android.translator.youdao.YDTranslate;

public class ComposeMessagePresenterImpl implements ComposeMessageContact.IComposeMessagePresenter {

    private static final String TAG = "RTranslator/ComposeMessagePresenterImpl";

    private ComposeMessageContact.IComposeMessageView mComposeMessageView;

    private StringBuilder sttString = new StringBuilder();

    private TranslatorStorage mTranslatorStorage;

    private UserBean mUserInfo;

    private ISTT mSTT;

    private ITTS mTTS;

    private IMPushApi mPushApi;

    private String mLanguage;

    private ITranslate mTranslate;

    private TTSManager mTTSManager;

    private int mThreadId = 0;

    private boolean isAuto;

    private MessageBean tmpMessage;

    private TStorageManager mStorageManager;

    public ComposeMessagePresenterImpl(String language, ComposeMessageContact.IComposeMessageView
            view, Activity activity) {
        mComposeMessageView = view;
        mTranslatorStorage = TranslatorStorage.getInstance();
        mUserInfo = mTranslatorStorage.getUser();
        mLanguage = language;
        mStorageManager = TStorageManager.getInstance();
        isAuto = mStorageManager.isAuto();

        initSTT(activity);
        initTTS(activity);
        initTranslate();
        initMPush(activity);
    }

    @Override
    public void initSTT(Activity activity) {
        mSTT = STTFactory.createSTT(activity, "iflytek");
        mSTT.setSTTFinishedListener(mSTTFinishedListener);
        mSTT.setSTTVoiceLevelListener(mSTTVoiceLevelListener);
        mSTT.setLanguageCode(mUserInfo.getLanguage());
    }

    @Override
    public void initTTS(Activity activity) {
        //mTTS = TTSManager.createTTS(this,"iflytek");
        mTTSManager = TTSManager.getInstance(activity);
        mTTS = mTTSManager.getTTS(mLanguage, true);
        mTTS.setTTSEventListener(mTTSEventListener);
    }

    @Override
    public void initMPush(Activity activity) {
        mPushApi = MPushApi.get(activity);
        mPushApi.startPush(Utils.getDeviceId(activity));
        mPushApi.setHttpCallBack(new HttpProxyCallback() {
            @Override
            public void onResponse(HttpResponse httpResponse) {
                Logger.i(TAG, "MPushApi - onResponse");
            }

            @Override
            public void onCancelled() {
                Logger.i(TAG, "MPushApi - onCancelled");
            }
        });

        mPushApi.setHttpClientListener(new HttpClientListener() {
            @Override
            public void onReceivePush(Client client, MessageBean messageBean, int i) {
                Logger.v(TAG, "mPushApi - onReceivePush:" + messageBean.getThreadId());
                if (messageBean.getThreadId() == mThreadId) {
                    Logger.v(TAG, "message language:" + messageBean.getLanguage());
                    Logger.v(TAG, "my language:" + mUserInfo.getLanguage());
                    mTranslate.doTranslate(messageBean, mUserInfo.getLanguage(), i);
                }
            }
        });
    }

    @Override
    public void initTranslate() {
        mTranslate = new YDTranslate();
        mTranslate.setTranslateFinishedListener(mTranslateFinishedListener);
    }

    @Override
    public void setAuto() {
        isAuto = !isAuto;
        mStorageManager.setAuto(isAuto);
    }

    @Override
    public boolean getAuto() {
        return isAuto;
    }

    @Override
    public void setTextSize(float size) {
        mStorageManager.setTextSize(size);
    }

    @Override
    public float getTextSize() {
        return mStorageManager.getTextSize();
    }


    @Override
    public void recordStart() {
        sttString = new StringBuilder();
        mSTT.startWithMicrophone();
    }

    @Override
    public void recordFinish() {
        mSTT.stopWithMicrophone();
        tmpMessage = MessageBean.create(0, 1, System.currentTimeMillis(), 1,
                MessageType.TYPE_TEXT, "", "", 0, mUserInfo.getLanguage());
    }

    @Override
    public void onResume() {
        mSTT.onResume();
        MediaManager.resume();
        mPushApi.resumePush();
    }

    @Override
    public void onPause() {
        mSTT.onPause();
        MediaManager.pause();
        mPushApi.pausePush();
    }

    @Override
    public void onDestroy() {
        if (mComposeMessageView != null) {
            mComposeMessageView = null;
        }
        MediaManager.release();
        mSTT.onDestroy();
        mTTS.release();
        if (mTTSManager != null) {
            mTTSManager.releaseAll();
        }
        System.gc();
    }

    ISTTFinishedListener mSTTFinishedListener = new ISTTFinishedListener() {
        @Override
        public void onSTTFinish(ISTT.FinalResponseStatus status, String text) {
            Logger.v(TAG, "onSTTFinish, status=" + status + ", text=" + text);

            if (ISTT.FinalResponseStatus.OK == status) {
                sttString.append(text);
            } else if (ISTT.FinalResponseStatus.NotReceived == status) {
                sttString.append(text);
            } else if (ISTT.FinalResponseStatus.Timeout == status) {
                sttString.append("");
            } else if (ISTT.FinalResponseStatus.Finished == status) {
                tmpMessage.setText(sttString.toString());
                mPushApi.sendPush(tmpMessage);
                if (mComposeMessageView != null) {
                    mComposeMessageView.callbackSTT(tmpMessage);
                }
            } else if (ISTT.FinalResponseStatus.Error == status) {
                if (mComposeMessageView != null) {
                    mComposeMessageView.callbackSTT(null);
                }
            }
        }
    };

    ISTTVoiceLevelListener mSTTVoiceLevelListener = new ISTTVoiceLevelListener() {
        @Override
        public void updateVoiceLevel(int level) {
            if (mComposeMessageView != null) {
                mComposeMessageView.updateVoiceLevel(level);
            }
        }
    };

    ITTSListener mTTSEventListener = new ITTSListener() {
        @Override
        public void onTTSEvent(int status, String info) {
            Logger.v(TAG, "onTTSEvent-" + status);
            if (mComposeMessageView != null) {
                mComposeMessageView.callbackTTS(status);
            }
        }
    };

    ITranslateFinishedListener mTranslateFinishedListener = new ITranslateFinishedListener() {
        @Override
        public void onTranslateFinish(MessageBean messageBean, int i) {
            Logger.v(TAG, "onTranslateFinish:" + i);
            mTTS.setVoice(messageBean.getLanguage(), false, true);
            String fileName = String.valueOf(System.currentTimeMillis());
            if (isAuto) {
                mTTS.speak(messageBean.getText(), fileName, AppContext.SOUND_FILE_DIR);
            } else {
                mTTS.synthesizeToUri(messageBean.getText(), fileName, AppContext.SOUND_FILE_DIR);
            }
            messageBean.setType(MessageType.TYPE_SOUND_TEXT);
            messageBean.setMemberId(1);
            messageBean.setUrl(AppContext.SOUND_ABSOLUTE_FILE_DIR + "/" + fileName + ".wav");
            if (mComposeMessageView != null) {
                mComposeMessageView.receiveMessage(messageBean);
            }
        }
    };
}
