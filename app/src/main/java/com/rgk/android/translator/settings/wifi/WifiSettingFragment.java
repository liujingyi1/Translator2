package com.rgk.android.translator.settings.wifi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.Status;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.rgk.android.translator.R;

public class WifiSettingFragment extends Fragment implements DialogInterface.OnClickListener{
    // Combo scans can take 5-6s to complete - set to 10s.
    private static final int WIFI_RESCAN_INTERVAL_MS = 10 * 1000;

    public Switch wifiSwitch;
    private TextView mTvWiFiState;
    private ListView mLvWiFi;
    private WiFiAdapter mWiFiAdapter;

    private IntentFilter mFilter;
    private BroadcastReceiver mReceiver;
    private Scanner mScanner;

    private WifiManager mWifiManager;
    private DetailedState mLastState;
    private WifiInfo mLastInfo;
    private int mLastPriority;
    private AccessPoint mSelected;
    private boolean mResetNetworks = false;
    private boolean mStateChangeWiFi = false;

    private WifiDialog mDialog;

    public WifiSettingFragment() {
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleEvent(intent);
            }
        };

        mScanner = new Scanner();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return  inflater.inflate(R.layout.wifi_settings, container,false);
    }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);
        findViews(view);
        init();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mReceiver, mFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);
        mScanner.pause();
        if (mResetNetworks) {
            enableNetworks();
        }
    }

    private void findViews(View view) {
        wifiSwitch = (Switch)view.findViewById(R.id.switch_status);
        mLvWiFi = (ListView)view.findViewById(R.id.lv_wifi);
        mTvWiFiState = (TextView)view.findViewById(R.id.tv_wifi_state);
    }

    private void init() {
        mWifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        mWiFiAdapter = new WiFiAdapter(getActivity());
        mLvWiFi.setAdapter(mWiFiAdapter);
        mLvWiFi.setOnItemClickListener(mWiFiAdapter);

        wifiSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (buttonView.isChecked()){
                    if (!mWifiManager.isWifiEnabled()) {
                        mWifiManager.setWifiEnabled(true);
                    }
                    mLvWiFi.setVisibility(View.VISIBLE);
                    mWifiManager.startScan();
                } else {
                    if (mWifiManager.isWifiEnabled()) {
                        mWifiManager.setWifiEnabled(false);
                    }
                    mLvWiFi.setVisibility(View.GONE);

                }
            }
        });
    }

    private void showDialog(AccessPoint accessPoint) {
        if (mDialog != null) {
            mDialog.dismiss();
        }
        mDialog = new WifiDialog(getContext(), this, accessPoint);
        mDialog.show();
    }

    private void forget(int networkId) {
        //Log.d("guoshuai","forget start");
        mWifiManager.removeNetwork(networkId);
        //Log.d("guoshuai","networkId = " + networkId);
        saveNetworks();
    }

    private void connect(int networkId) {
        if (networkId == -1) {
            return;
        }

        // Reset the priority of each network if it goes too high.
        if (mLastPriority > 1000000) {
            final ArrayList<AccessPoint> accessPointList = mWiFiAdapter.getAccessPointList();
            if (null == accessPointList) {
                return;
            }

            for (int i = accessPointList.size() - 1; i >= 0; --i) {
                AccessPoint accessPoint = accessPointList.get(i);
                if (accessPoint.networkId != -1) {
                    WifiConfiguration config = new WifiConfiguration();
                    config.networkId = accessPoint.networkId;
                    config.priority = 0;
                    mWifiManager.updateNetwork(config);
                }
            }
            mLastPriority = 0;
        }

        // Set to the highest priority and save the configuration.
        WifiConfiguration config = new WifiConfiguration();
        config.networkId = networkId;
        config.priority = ++mLastPriority;
        mWifiManager.updateNetwork(config);
        saveNetworks();

        // Connect to network by disabling others.
        mWifiManager.enableNetwork(networkId, true);
        mWifiManager.reconnect();
        mResetNetworks = true;
    }

    private void enableNetworks() {
        final ArrayList<AccessPoint> accessPointList = mWiFiAdapter.getAccessPointList();
        if (null == accessPointList) {
            return;
        }

        for (int i = accessPointList.size() - 1; i >= 0; --i) {
            WifiConfiguration config = accessPointList.get(i).getConfig();
            if (config != null && config.status != Status.ENABLED) {
                mWifiManager.enableNetwork(config.networkId, false);
            }
        }
        mResetNetworks = false;
    }

    private void saveNetworks() {
        // Always save the configuration with all networks enabled.
        enableNetworks();
        mWifiManager.saveConfiguration();
        updateAccessPoints();
    }

    private void handleEvent(Intent intent) {
        String action = intent.getAction();
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            updateWifiState(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN));
        } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            updateAccessPoints();
        } else if (WifiManager.NETWORK_IDS_CHANGED_ACTION.equals(action)) {
            if (mSelected != null && mSelected.networkId != -1) {
                mSelected = null;
            }
            updateAccessPoints();
        } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
            updateConnectionState(WifiInfo.getDetailedStateOf((SupplicantState)
                    intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            updateConnectionState(((NetworkInfo) intent.getParcelableExtra(
                    WifiManager.EXTRA_NETWORK_INFO)).getDetailedState());
        } else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
            updateConnectionState(null);
        }
    }

    private void updateConnectionState(DetailedState state) {
        /* sticky broadcasts can call this when wifi is disabled */
        if (!mWifiManager.isWifiEnabled()) {
            mScanner.pause();
            return;
        }

        if (state == DetailedState.OBTAINING_IPADDR) {
            mScanner.pause();
        } else {
            mScanner.resume();
        }

        mLastInfo = mWifiManager.getConnectionInfo();
        if (state != null) {
            mLastState = state;
        }

        final ArrayList<AccessPoint> accessPointList = mWiFiAdapter.getAccessPointList();
        if (null == accessPointList) {
            return;
        }

        boolean update = false;
        for (int i = accessPointList.size() - 1; i >= 0; --i) {
            if (accessPointList.get(i).update(mLastInfo, mLastState)) {
                update = true;
            }
        }

        if (update) {
            mWiFiAdapter.sort();
            changeWiFiConnectInfo();
        }
    }

    private void updateWifiState(int state) {
        if (state == WifiManager.WIFI_STATE_ENABLED) {
            mScanner.resume();
            updateAccessPoints();
        } else {
            mScanner.pause();
            mWiFiAdapter.setAccessPointList(null);
        }

        switch (state) {
            case WifiManager.WIFI_STATE_ENABLING:
                showWiFiState(R.string.wifi_starting);
                wifiSwitch.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                setWiFiSwitch(true);
                wifiSwitch.setEnabled(true);
                mScanner.resume();
                updateAccessPoints();
                return;
            case WifiManager.WIFI_STATE_DISABLING:
                showWiFiState(R.string.wifi_stopping);
                wifiSwitch.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                setWiFiSwitch(false);
                showWiFiState(R.string.wifi_closed);
                wifiSwitch.setEnabled(true);
                mScanner.pause();
                mWiFiAdapter.setAccessPointList(null);
                break;
            default:
                setWiFiSwitch(false);
                wifiSwitch.setEnabled(true);
        }

        mLastInfo = null;
        mLastState = null;
        mScanner.pause();
    }

    private void setWiFiSwitch(boolean switched) {
        mStateChangeWiFi = true;
        wifiSwitch.setChecked(switched);
        mStateChangeWiFi = false;
    }

    private void showWiFiState(int resid) {
        mTvWiFiState.setText(resid);
    }

    private void updateAccessPoints() {
        ArrayList<AccessPoint> accessPoints = new ArrayList<AccessPoint>();

        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (configs != null) {
            mLastPriority = 0;
            for (WifiConfiguration config : configs) {
                if (config.priority > mLastPriority) {
                    mLastPriority = config.priority;
                }

                // Shift the status to make enableNetworks() more efficient.
                if (config.status == Status.CURRENT) {
                    config.status = Status.ENABLED;
                } else if (mResetNetworks && config.status == Status.DISABLED) {
                    config.status = Status.CURRENT;
                }

                AccessPoint accessPoint = new AccessPoint(getActivity(), config);
                accessPoint.update(mLastInfo, mLastState);
                accessPoints.add(accessPoint);
            }
        }

        List<ScanResult> results = mWifiManager.getScanResults();
        if (results != null) {
            for (ScanResult result : results) {
                // Ignore hidden and ad-hoc networks.
                if (result.SSID == null || result.SSID.length() == 0 ||
                        result.capabilities.contains("[IBSS]")) {
                    continue;
                }

                boolean found = false;
                for (AccessPoint accessPoint : accessPoints) {
                    if (accessPoint.update(result)) {
                        found = true;
                    }
                }
                if (!found) {
                    accessPoints.add(new AccessPoint(getActivity(), result));
                }
            }
        }

        mWiFiAdapter.setAccessPointList(accessPoints);
        changeWiFiConnectInfo();
    }

    private class Scanner extends Handler {
        private int mRetry = 0;

        void resume() {
            if (!hasMessages(0)) {
                sendEmptyMessage(0);
            }
        }

        void pause() {
            mRetry = 0;
            removeMessages(0);
        }

        @Override
        public void handleMessage(Message message) {
            if (mWifiManager.startScan()) {
                mRetry = 0;
            } else if (++mRetry >= 3) {
                mRetry = 0;
                Toast.makeText(getActivity(), R.string.wifi_fail_to_scan,
                        Toast.LENGTH_LONG).show();
                return;
            }
            sendEmptyMessageDelayed(0, WIFI_RESCAN_INTERVAL_MS);
        }
    }

    private class WiFiAdapter extends BaseAdapter implements OnItemClickListener {

        private final Context mContext;
        private ArrayList<AccessPoint> mAccessPointList = null;

        public WiFiAdapter(Context context) {
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return mAccessPointList == null ? 0 : mAccessPointList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            WiFiHolder wiFiHolder;
            if (null == convertView) {
                wiFiHolder = new WiFiHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.wifi_list_item, null);
                wiFiHolder.mTvTitle = (TextView) convertView.findViewById(R.id.tv_title);
                wiFiHolder.mTvSummary = (TextView) convertView.findViewById(R.id.tv_summary);
                wiFiHolder.mIvSignal = (ImageView) convertView.findViewById(R.id.iv_signal);
                convertView.setTag(wiFiHolder);
            } else {
                wiFiHolder = (WiFiHolder) convertView.getTag();
            }

            AccessPoint accessPoint = mAccessPointList.get(position);
            wiFiHolder.mTvTitle.setText(accessPoint.ssid);
            wiFiHolder.mTvSummary.setText(accessPoint.summary);
            accessPoint.setImageSignal(wiFiHolder.mIvSignal);

            return convertView;
        }

        void setAccessPointList(ArrayList<AccessPoint> accessPointList) {
            this.mAccessPointList = accessPointList;
            sort();
        }

        ArrayList<AccessPoint> getAccessPointList() {
            return mAccessPointList;
        }

        private void sort() {
            if (mAccessPointList != null) {
                Collections.sort(mAccessPointList);
                notifyDataSetChanged();
            }
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            mSelected = mAccessPointList.get(position);
            showDialog(mSelected);
        }

        class WiFiHolder {
            TextView mTvTitle;
            TextView mTvSummary;
            ImageView mIvSignal;
        }

    }

    @Override
    public void onClick(DialogInterface dialog, int button) {
        if (button == WifiDialog.BUTTON_FORGET && mSelected != null) {
            forget(mSelected.networkId);
        } else if (button == WifiDialog.BUTTON_SUBMIT && mDialog != null) {
            WifiConfiguration config = mDialog.getConfig();

            if (config == null) {
                if (mSelected != null) {
                    connect(mSelected.networkId);
                }
            } else if (config.networkId != -1) {
                if (mSelected != null) {
                    mWifiManager.updateNetwork(config);
                    saveNetworks();
                }
            } else {
                int networkId = mWifiManager.addNetwork(config);
                if (networkId != -1) {
                    mWifiManager.enableNetwork(networkId, false);
                    config.networkId = networkId;
                    connect(networkId);
                }
            }
        }
    }

    private void changeWiFiConnectInfo() {
        final ArrayList<AccessPoint> accessPointList = mWiFiAdapter.getAccessPointList();
        if (null == accessPointList || accessPointList.size() < 1) {
            return;
        }

        boolean change = false;
        AccessPoint accessPoint;
        for (int i = accessPointList.size() - 1; i >= 0; --i) {
            accessPoint = accessPointList.get(i);
            if (accessPoint.getInfo() != null) {
                change = true;
            }
        }

        if (!change) {
        }
    }

}
