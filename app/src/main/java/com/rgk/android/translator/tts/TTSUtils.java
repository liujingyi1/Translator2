package com.rgk.android.translator.tts;

public class TTSUtils {

    public static boolean isIflytekSupport(String language, boolean male) {
        if ("en-US".equals(language) || "en-AU".equals(language) || "en-GB".equals(language)
                ||"en-NZ".equals(language) ||"en-IN".equals(language) || "zh-CN".equals(language)
                || "zh-TW".equals(language) || "zh-HK".equals(language) || "es-ES".equals(language)
                || "hi-IN".equals(language) || "vi-VN".equals(language) || "ru-RU".equals(language)
                || "fr-FR".equals(language)) {
            return true;
        }
        return false;
    }

    public static int getTTSCount() {
        return 2;
    }
}

