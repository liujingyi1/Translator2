package com.rgk.android.translator.settings.storage;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.rgk.android.translator.R;
import com.rgk.android.translator.storage.TStorageManager;
import com.rgk.android.translator.utils.Logger;

public class StorageFragment extends Fragment implements View.OnClickListener {
    private RadioGroup mRadioGroup;
    private Button mSaveButton;
    private Button mClearDataButton;
    private int mStorageType = 1;

    private static final String KEY_STORAGE_TYPE = "storage_type";

    private interface StorageType {
        int TYPE_NOT_SAVE = 1;
        int TYPE_SAVE_TEXT = 2;
        int TYPE_SAVE_SOUND = 3;
        int TYPE_SAVE_TEXT_SOUND = 4;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.setting_storage, container, false);
        mRadioGroup = v.findViewById(R.id.save_type);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.not_save:
                        mStorageType = StorageType.TYPE_NOT_SAVE;
                        break;
                    case R.id.only_save_text:
                        mStorageType = StorageType.TYPE_SAVE_TEXT;
                        break;
                    case R.id.only_save_sound:
                        mStorageType = StorageType.TYPE_SAVE_SOUND;
                        break;
                    case R.id.both_save:
                        mStorageType = StorageType.TYPE_SAVE_TEXT_SOUND;
                        break;
                }
            }
        });
        mSaveButton = v.findViewById(R.id.save_button);
        mSaveButton.setOnClickListener(this);
        mClearDataButton = v.findViewById(R.id.clear_data_button);
        mClearDataButton.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_button:
                TStorageManager.getInstance().setStorageType(mStorageType);
                break;
            case R.id.clear_data_button:
                break;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_STORAGE_TYPE, mStorageType);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mStorageType = savedInstanceState.getInt(KEY_STORAGE_TYPE);
        } else {
            mStorageType = TStorageManager.getInstance().getStorageType();
        }
        RadioButton radioButton;
        switch (mStorageType) {
            case StorageType.TYPE_NOT_SAVE:
                radioButton = getView().findViewById(R.id.not_save);
                radioButton.setChecked(true);
                break;
            case StorageType.TYPE_SAVE_TEXT:
                radioButton = getView().findViewById(R.id.only_save_text);
                radioButton.setChecked(true);
                break;
            case StorageType.TYPE_SAVE_SOUND:
                radioButton = getView().findViewById(R.id.only_save_sound);
                radioButton.setChecked(true);
                break;
            case StorageType.TYPE_SAVE_TEXT_SOUND:
                radioButton = getView().findViewById(R.id.both_save);
                radioButton.setChecked(true);
                break;
        }
    }
}
