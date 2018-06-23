package com.rgk.android.translator.tts;

import android.app.Activity;
import android.content.Context;

import com.rgk.android.translator.iflytek.tts.FTTTS;
import com.rgk.android.translator.microsoft.tts.MSTTS;
import com.rgk.android.translator.utils.Logger;


public class TTSManager {

    private static final String TAG = "RTranslator/TTSManager";
    private static TTSManager mInstance;
    private static int TTS_IFLYTEK = 0;
    private static int TTS_MICROSOFT = 1;
    private static int TTS_COUNT = 2;
    private static String IFLYTEK = "iflytek";
    private static String MICROSOFT = "microsoft";

    String[] types = new String[]{IFLYTEK,MICROSOFT};
    private static ITTS[] itts = new ITTS[TTS_COUNT];

    private TTSManager(Context context) {
        int i = 0;
        for (String type : types) {
            ITTS tts = null;
            if (IFLYTEK.equals(type)) {
                tts = (ITTS) new FTTTS(context);
            }  else if (MICROSOFT.equals(type)) {
                tts = (ITTS) new MSTTS(context);
            }
            itts[i] = tts;
            i++;
        }
    }

    public static TTSManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (TTSManager.class) {
                if (mInstance == null) {
                    mInstance = new TTSManager(context);
                }
            }
        }
        return mInstance;
    }

    public ITTS getTTS(String language, boolean male) {
        Logger.d(TAG,"lang : " + language + " male : " + male);
        if (TTSUtils.isIflytekSupport(language, male)) {
            return itts[TTS_IFLYTEK];
        } else {
            return itts[TTS_MICROSOFT];
        }
    }

    public ITTS getDefaultTTS() {
        return itts[TTS_IFLYTEK];
    }

    public boolean isIflytek(ITTS tts) {
        return (tts instanceof FTTTS);
    }

    public void releaseAll() {
        for (ITTS itt : itts) {
            if (itt != null) {
                itt.release();
            }
        }
    }

}
