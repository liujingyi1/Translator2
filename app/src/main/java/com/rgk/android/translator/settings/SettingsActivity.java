package com.rgk.android.translator.settings;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;

import com.rgk.android.translator.R;
import com.rgk.android.translator.settings.about.AboutFragment;
import com.rgk.android.translator.settings.common.CommonSettingFragment;
import com.rgk.android.translator.settings.ota.OtaFragment;
import com.rgk.android.translator.settings.ota.OtaTools;
import com.rgk.android.translator.settings.ota.UpdateCheckTask;
import com.rgk.android.translator.settings.pair.PairSettingFragment;
import com.rgk.android.translator.settings.role.RoleSettingFragment;
import com.rgk.android.translator.settings.storage.StorageFragment;
import com.rgk.android.translator.settings.wifi.WifiSettingFragment;
import com.rgk.android.translator.utils.Logger;

import java.util.ArrayList;

public class SettingsActivity extends Activity implements View.OnClickListener, UpdateCheckTask.OnCheckListener {
    private static final String TAG = "RTranslator/SettingsActivity";
    private SparseArray<Class> mSparseArray;
    private SparseArray<SettingButton> mSettingButtons;
    private View mCurrentSettingButton;
    private ImageView mBackButton;

    private static final String KEY_CURRENT_FRAGMENT = "current_fragment";

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        registerFragment();
        initViews();
        if (savedInstanceState != null) {
            int viewId = savedInstanceState.getInt(KEY_CURRENT_FRAGMENT);
            mCurrentSettingButton = mSettingButtons.get(viewId);
            mCurrentSettingButton.setSelected(true);
            /*switchToFragment(getFragment(viewId));*/
        } else {
            String action = getIntent().getAction();
            Logger.d(TAG, "action: " + action);
            int viewId = getViewId(action);
            mCurrentSettingButton = mSettingButtons.get(viewId);
            mCurrentSettingButton.setSelected(true);
            switchToFragment(getFragment(viewId));
        }

        new UpdateCheckTask(this, this).execute();
    }

    private int getViewId(String action) {
        int viewId = R.id.wifi_setting_button;
        if (action == null) {
            return viewId;
        }
        switch (action) {
            case "com.translator.setting.network":
                viewId = R.id.wifi_setting_button;
                break;

            case "com.translator.setting.pair":
                viewId = R.id.pair_setting_button;
                break;

            case "com.translator.setting.role":
                viewId = R.id.role_setting_button;
                break;

            case "com.translator.setting.common":
                viewId = R.id.common_setting_button;
                break;

            case "com.translator.setting.storage":
                viewId = R.id.storage_setting_button;
                break;

            case "com.translator.setting.ota":
                viewId = R.id.ota_setting_button;
                break;

            case "com.translator.setting.about":
                viewId = R.id.about_setting_button;
                break;

            case "com.translator.setting.mainscreen":
                viewId = R.id.wifi_setting_button;
                //TODO modify right view id
                break;
        }
        return viewId;
    }

    private void initViews() {
        mBackButton = findViewById(R.id.back_button);
        mBackButton.setOnClickListener(this);
        mSettingButtons = new SparseArray<>();
        initWifiSettingButton();
        initPairSettingButton();
        initRoleSettingButton();
        initCommonSettingButton();
        initStorageSettingButton();
        initOtaSettingButton();
        initAboutSettingButton();
        int size = mSettingButtons.size();
        for (int i = 0; i < size; i++) {
            mSettingButtons.valueAt(i).setOnClickListener(this);
        }
    }

    private void initWifiSettingButton() {
        SettingButton settingButton = findViewById(R.id.wifi_setting_button);
        settingButton.setIcon(R.drawable.setting_wifi_button_drawable);
        settingButton.setLabel(R.string.wifi_setting_button_label);
        mSettingButtons.put(R.id.wifi_setting_button, settingButton);
    }

    private void initPairSettingButton() {
        SettingButton settingButton = findViewById(R.id.pair_setting_button);
        settingButton.setIcon(R.drawable.setting_pair_button_drawable);
        settingButton.setLabel(R.string.pair_setting_button_label);
        mSettingButtons.put(R.id.pair_setting_button, settingButton);
    }

    private void initRoleSettingButton() {
        SettingButton settingButton = findViewById(R.id.role_setting_button);
        settingButton.setIcon(R.drawable.setting_role_button_drawable);
        settingButton.setLabel(R.string.role_setting_button_label);
        mSettingButtons.put(R.id.role_setting_button, settingButton);
    }

    private void initCommonSettingButton() {
        SettingButton settingButton = findViewById(R.id.common_setting_button);
        settingButton.setIcon(R.drawable.setting_common_button_drawable);
        settingButton.setLabel(R.string.common_setting_button_label);
        mSettingButtons.put(R.id.common_setting_button, settingButton);
    }

    private void initStorageSettingButton() {
        SettingButton settingButton = findViewById(R.id.storage_setting_button);
        settingButton.setIcon(R.drawable.setting_storage_button_drawable);
        settingButton.setLabel(R.string.storage_setting_button_label);
        mSettingButtons.put(R.id.storage_setting_button, settingButton);
    }

    private void initOtaSettingButton() {
        SettingButton settingButton = findViewById(R.id.ota_setting_button);
        settingButton.setIcon(R.drawable.setting_ota_button_drawable);
        settingButton.setLabel(R.string.ota_setting_button_label);
        mSettingButtons.put(R.id.ota_setting_button, settingButton);
    }

    private void initAboutSettingButton() {
        SettingButton settingButton = findViewById(R.id.about_setting_button);
        settingButton.setIcon(R.drawable.setting_about_button_drawable);
        settingButton.setLabel(R.string.about_setting_button_label);
        mSettingButtons.put(R.id.about_setting_button, settingButton);
    }

    private void registerFragment() {
        mSparseArray = new SparseArray<>();
        mSparseArray.put(R.id.wifi_setting_button, WifiSettingFragment.class);
        mSparseArray.put(R.id.pair_setting_button, PairSettingFragment.class);
        mSparseArray.put(R.id.role_setting_button, RoleSettingFragment.class);
        mSparseArray.put(R.id.common_setting_button, CommonSettingFragment.class);
        mSparseArray.put(R.id.storage_setting_button, StorageFragment.class);
        mSparseArray.put(R.id.ota_setting_button, OtaFragment.class);
        mSparseArray.put(R.id.about_setting_button, AboutFragment.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_FRAGMENT, mCurrentSettingButton.getId());
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSettingButtons.clear();
        mSparseArray.clear();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_button:
                finish();
                return;
        }
        
        if (mCurrentSettingButton == v) {
            Logger.d(TAG, "onClick(): repeat click return.");
            return;
        }
        Fragment fragment = getFragment(v.getId());
        if (fragment == null) {
            Logger.d(TAG, "onClick(): fragment is null.");
            return;
        }
        mCurrentSettingButton.setSelected(false);
        v.setSelected(true);
        mCurrentSettingButton = v;
        switchToFragment(fragment);
    }

    private Fragment getFragment(int viewId) {
        Class cls = mSparseArray.get(viewId);
        Fragment fragment = null;
        try {
            fragment = (Fragment) cls.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return fragment;
    }

    private void switchToFragment(Fragment fragment) {
        Logger.d(TAG, "switchToFragment(): to " + fragment.getClass().getName());
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.setting_panel, fragment);
        //transaction.commitAllowingStateLoss();
        transaction.commit();
        getFragmentManager().executePendingTransactions();
    }

    @Override
    public void preCheck() {

    }

    @Override
    public void onSuccess(UpdateCheckTask.UpdateInfo info) {
        if (this.isFinishing()) return;
        String versionName = info.getVersionName();
        String version = OtaTools.getVersionName(this);
        if (!versionName.equals(version)) {
            mSettingButtons.get(R.id.ota_setting_button).setIcon(R.drawable.setting_ota_button_drawable);
        }
    }

    @Override
    public void onFailed() {

    }
}
