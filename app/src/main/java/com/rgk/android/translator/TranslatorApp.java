package com.rgk.android.translator;

import android.app.Application;
import android.content.Context;

import com.rgk.android.translator.storage.TStorageManager;
import com.rgk.android.translator.tts.TTSManager;

import butterknife.ButterKnife;

public class TranslatorApp extends Application {
    public TStorageManager mStorageManager;
    private static TranslatorApp mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        TStorageManager.init(this);
        mStorageManager = TStorageManager.getInstance();
        TTSManager.getInstance(this);

                mInstance = this;
                ButterKnife.setDebug(true);
    }

        public static TranslatorApp getInstance() {
               return mInstance;
            }
        public static Context getAppContext() {
                return mInstance.getApplicationContext();
    }
}
