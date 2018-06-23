package com.rgk.android.translator.settings.ota;

import android.content.Context;
import android.os.AsyncTask;
import com.rgk.android.translator.utils.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateCheckTask extends AsyncTask<Void, Void, String> {

    private final String TAG = "RTranslator/UpdateCheckTask";
    Context mContext;
    OnCheckListener mListener = null;

    public UpdateCheckTask(Context context, OnCheckListener listener) {
        this.mContext = context;
        this.mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        Logger.i(TAG, "onPreExecute()");
        if (mListener != null) {
            mListener.preCheck();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        Logger.i(TAG, "onPostExecute() : " + result);
//        UpdateInfo info1 = new UpdateInfo("我是测试版本信息", "www.rgktranslator.com", "2.0.1",
//                "dada", false, false);
        if (result == null || result.length() == 0) {
            if (this.mListener != null) {
                this.mListener.onFailed();
            }
        } else {
            UpdateInfo info = parseJson(result);
            if (info == null) {
                if (this.mListener != null) {
                    this.mListener.onFailed();
                }
            } else {
                if (this.mListener != null) {
                    this.mListener.onSuccess(info);
                }
            }
        }

//        if (this.mListener != null) {
//            this.mListener.onSuccess(info1);
//        }
    }

    @Override
    protected String doInBackground(Void... voids) {
        Logger.i(TAG, "doInBackground()");
        HttpURLConnection uRLConnection = null;
        InputStream is = null;
        BufferedReader buffer = null;
        String result = null;
        String urlStr = OtaConstants.UPDATE_REQUEST_URL + "?" + OtaConstants.APK_VERSION_NAME + "=" + OtaTools.getVersionName(this.mContext);

        try {
            URL url = new URL(urlStr);
            uRLConnection = (HttpURLConnection) url.openConnection();
            uRLConnection.setRequestMethod("GET");
            uRLConnection.setConnectTimeout(5000);
            uRLConnection.setReadTimeout(5000);
            is = uRLConnection.getInputStream();
            buffer = new BufferedReader(new InputStreamReader(is));
            StringBuilder strBuilder = new StringBuilder();
            String line;
            while ((line = buffer.readLine()) != null) {
                strBuilder.append(line);
            }
            result = strBuilder.toString();
        } catch (Exception e) {
            Logger.e(TAG, "http post error");
        } finally {
            try {
                if (buffer != null) {
                    buffer.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
            }
            Logger.e(TAG, "http disconnect");
            if (uRLConnection != null) {
                uRLConnection.disconnect();
            }
        }

        if (result != null) {
            Logger.i(TAG, result);
        }

        return result;
    }


    private UpdateInfo parseJson(String result) {
        UpdateInfo info = null;
        try {
            JSONObject obj = new JSONObject(result);
            String updateMessage = obj.getString(OtaConstants.APK_UPDATE_CONTENT);
            String apkUrl = obj.getString(OtaConstants.APK_DOWNLOAD_URL);
            String versionName = obj.getString(OtaConstants.APK_VERSION_NAME);
            String md5 = obj.getString(OtaConstants.APK_MD5);
            boolean diffUpdate = obj.getBoolean(OtaConstants.APK_DIFF_UPDATE);
            boolean forceUpdate = obj.getBoolean(OtaConstants.APK_FORCE_UPDATE);

            String current = OtaTools.getVersionName(mContext);
            //if (!current.equals(versionName)) {
            info = new UpdateInfo(updateMessage, apkUrl, versionName, md5, diffUpdate, forceUpdate);
            //}

        } catch (JSONException e) {
            Logger.e(TAG, "parse json error");
        }
        return info;
    }

    public interface OnCheckListener {
        void preCheck();
        void onSuccess(UpdateInfo info);
        void onFailed();
    }


    public class UpdateInfo {

        private String mMessage;
        private String mUrl;
        private String mVersionName;
        private String mMD5;
        private boolean mDiffUpdate;
        private boolean mForceUpdate;

        UpdateInfo(String msg, String url, String versionName, String md5, boolean diffUpdate, boolean forceUpdate) {
            this.mMessage = msg;
            this.mUrl = url;
            this.mVersionName = versionName;
            this.mMD5 = md5;
            this.mDiffUpdate = diffUpdate;
            this.mForceUpdate = forceUpdate;
        }

        public String getMessage() {
            return mMessage;
        }

        public String getUrl() {
            return mUrl;
        }

        public String getVersionName() {
            return mVersionName;
        }

        public String getMD5() {
            return mMD5;
        }

        public boolean isDiffUpdate() {
            return mDiffUpdate;
        }

        public boolean isForceUpdate() {
            return mForceUpdate;
        }

    }

}
