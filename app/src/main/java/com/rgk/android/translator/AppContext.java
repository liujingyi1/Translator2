package com.rgk.android.translator;

import android.os.Environment;

public class AppContext {
    public static final String SOUND_FILE_DIR = "rtranslator";
    public static final String SOUND_ABSOLUTE_FILE_DIR = Environment.getExternalStorageDirectory() + "/" + SOUND_FILE_DIR;

    public static final float TEXT_SIZE_SMALL = 26f;
    public static final float TEXT_SIZE_NORMAL = 32f;
    public static final float TEXT_SIZE_LARGE = 38f;

}
