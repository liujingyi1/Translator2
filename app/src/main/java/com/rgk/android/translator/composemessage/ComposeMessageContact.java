package com.rgk.android.translator.composemessage;

import android.app.Activity;

import com.rgk.android.translator.database.beans.MessageBean;
import com.rgk.android.translator.mvpbase.BasePresenter;

public class ComposeMessageContact {
    public interface IComposeMessageView {
        void callbackSTT(MessageBean messageBean);

        void updateVoiceLevel(int level);

        void receiveMessage(MessageBean messageBean);

        void callbackTTS(int status);
    }

    public interface IComposeMessagePresenter extends BasePresenter {
        void initSTT(Activity activity);

        void initTTS(Activity activity);

        void initMPush(Activity activity);

        void initTranslate();

        void setTextSize(float size);

        float getTextSize();

        void setAuto();

        boolean getAuto();

        void recordStart();

        void recordFinish();

        void onResume();

        void onPause();
    }
}
