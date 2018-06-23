package com.rgk.android.translator.settings.ota;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.rgk.android.translator.R;
import com.rgk.android.translator.utils.Logger;
import com.rgk.android.translator.utils.NetUtil;


public class OtaFragment extends Fragment implements UpdateCheckTask.OnCheckListener {

    private static final String TAG = "RTranslator/OtaFragment";
    private View mView;
    private TextView otaText, versionText;
    private Button otaButton;
    private ProgressBar progressBar;
    private ProgressDialog pBar;
    DownloadTask downloadTask;
    private PowerManager.WakeLock mWakeLock;
    private static final String DOWNLOAD_NAME = "ota/";
    private boolean isAttached = false;
    private String versionName;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Logger.d(TAG,"onCreateView ");
        mView = inflater.inflate(R.layout.activity_ota, container, false);
        initViews();
        checkOTA();
        return mView;
    }

    private void checkOTA() {
        if (!NetUtil.isNetworkConnected(getActivity())) {
            otaText.setText(R.string.ota_network_error);
            progressBar.setVisibility(View.GONE);
            return;
        }
        new UpdateCheckTask(getActivity(), this).execute();
    }

    private void initViews() {
        versionName = OtaTools.getVersionName(getActivity());
        progressBar = mView.findViewById(R.id.ota_progressBar);
        otaText = mView.findViewById(R.id.ota_textView);
        versionText = mView.findViewById(R.id.ota_version);
        versionText.setText(getString(R.string.ota_current_version) + "V" + versionName);
        otaButton = mView.findViewById(R.id.ota_button);
        otaButton.setVisibility(View.GONE);
        otaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.d(TAG,"update button click ");
                checkOTA();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.d(TAG,"onResume ");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        isAttached = true;
        Logger.d(TAG,"onAttach ");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        isAttached = false;
        Logger.d(TAG,"onDetach ");
    }

    @Override
    public void onStop() {
        super.onStop();
        Logger.d(TAG,"onStop ");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d(TAG,"onDestroy ");
        if (downloadTask != null) {
            downloadTask.cancel(true);
        }
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        if (pBar != null) {
            pBar.dismiss();
        }
    }

    @Override
    public void preCheck() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSuccess(UpdateCheckTask.UpdateInfo info) {
        Logger.d(TAG,"onSuccess info : " + info + " getActivity() : " + getActivity());
        if (!isAttached) return;
        progressBar.setVisibility(View.GONE);
        otaButton.setVisibility(View.VISIBLE);
        if (info != null) {
            boolean forceUpdate = info.isForceUpdate();
            boolean diffUpdate = info.isDiffUpdate();
            String versionName = info.getVersionName();
            String url = info.getUrl();
            String msg = info.getMessage();
            showMessageDialog(forceUpdate, diffUpdate, versionName, url, msg);
        } else {
            otaText.setText(R.string.ota_check_failed);
        }

    }

    @Override
    public void onFailed() {
        Logger.d(TAG, "onFailed " + isAttached);
        if (!isAttached) return;
        Toast.makeText(getActivity(), R.string.ota_check_failed,Toast.LENGTH_LONG).show();
        otaButton.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        otaText.setText(R.string.ota_update_failed);
    }

    private void showMessageDialog(boolean forceUpdate, final boolean diffUpdate, final String versionName, final String url, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle(getContext().getString(R.string.ota_new_version) + versionName);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ota_positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showDownloadDialog(diffUpdate,versionName,url);
            }
        });
        if (!forceUpdate) {
            builder.setNegativeButton(R.string.ota_negative_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
        builder.create().show();
    }

    private void showDownloadDialog(boolean diffUpdate, String versionName, String url) {
        Logger.d(TAG,"showDownloadDialog versionName : " + versionName + " url : " + url);
        pBar = new ProgressDialog(getContext());
        pBar.setCanceledOnTouchOutside(false);
        pBar.setTitle("应用更新");
        pBar.setMessage("正在下载最新版本");
        pBar.setIndeterminate(true);
        pBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pBar.setCancelable(true);
        downloadTask = new DownloadTask(
                getActivity());
        downloadTask.execute(url);
        pBar.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                downloadTask.cancel(true);
            }
        });
    }

    class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            File file = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.connect();
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP "
                            + connection.getResponseCode() + " "
                            + connection.getResponseMessage();
                }
                int fileLength = connection.getContentLength();
                if (Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {
                    file = new File(Environment.getExternalStorageDirectory(),
                            DOWNLOAD_NAME + "Ota.apk");
                    if (!file.exists()) {
                        if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }
                    } else {
                        file.delete();
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.ota_sd_error,
                            Toast.LENGTH_LONG).show();
                }
                input = connection.getInputStream();
                output = new FileOutputStream(file);
                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    if (fileLength > 0) {
                        publishProgress((int) (total * 100 / fileLength));
                    }
                    output.write(data, 0, count);

                }
            } catch (Exception e) {
                e.printStackTrace();
                return "Server connect error";
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException e) {
                }
                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            PowerManager pm = (PowerManager) context
                    .getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            pBar.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            pBar.setIndeterminate(false);
            pBar.setMax(100);
            pBar.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            pBar.dismiss();
            if (result != null) {
                Toast.makeText(getActivity(), R.string.ota_download_error,Toast.LENGTH_LONG).show();
            } else {
                OtaTools.updateApk(getActivity());
            }

        }
    }

}
