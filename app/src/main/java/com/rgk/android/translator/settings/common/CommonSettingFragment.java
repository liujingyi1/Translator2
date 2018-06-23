package com.rgk.android.translator.settings.common;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.backup.BackupManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.LocaleList;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;

import com.rgk.android.translator.R;
import com.rgk.android.translator.utils.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CommonSettingFragment extends Fragment {

    private Button mBrightnessLevel;
    private Switch mBatteryPercentage;
    private Spinner mSystemLanguages;
    private Spinner mTextSize;
    private Switch mSwitch_12_24;
    private Switch mAutoTimeZone;
    private Spinner mTimeZone;

    private static final String TAG = "CommonSettingFragment";
    private static final String BATTERY_PERCENTAGE_ENABLE = "battery_percentage_enable";
    private static final String INTENT = "android.intent.action.SHOW_BRIGHTNESS_DIALOG";
    public static final String EXTRA_TIME_PREF_24_HOUR_FORMAT =
            "android.intent.extra.TIME_PREF_24_HOUR_FORMAT";

    TimeZoneChangedReceiver mTimeZoneChangedReceiver;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Logger.d(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.setting_common, container, false);
        mBrightnessLevel = view.findViewById(R.id.bt_brightness_level);
        mSystemLanguages = view.findViewById(R.id.sp_system_languages);
        mTextSize = view.findViewById(R.id.sp_text_size);
        mBatteryPercentage = view.findViewById(R.id.sw_battery_percentage);
        mSwitch_12_24 = view.findViewById(R.id.sw_24_hour_format);
        mAutoTimeZone = view.findViewById(R.id.sw_auto_time_zone);
        mTimeZone = view.findViewById(R.id.sp_time_zone);
        /*configBrightnessLevel();
        configSystemLanguages();
        configSystemFontSize();
        configBatteryPercentage();
        configDateAndTime();*/
        return view;
    }

    @Override
    public void onStart() {
        Logger.d(TAG, "onStart: ");
        super.onStart();
        if (mTimeZoneChangedReceiver == null) {
            mTimeZoneChangedReceiver = new TimeZoneChangedReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        getContext().registerReceiver(mTimeZoneChangedReceiver, filter);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTimeZoneChangedReceiver != null) {
            getContext().unregisterReceiver(mTimeZoneChangedReceiver);
        }
    }

    private class TimeZoneChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == Intent.ACTION_TIMEZONE_CHANGED) {
                final String[][] timezones = getAllTimezones();
                TimeZone locale = TimeZone.getDefault();
                String localeID = locale.getID();
                String[] localeIDs = timezones[0];
                int index = 64;
                for (int i = 0; i < localeIDs.length; i++) {
                    if (localeID.endsWith(localeIDs[i])) {
                        index = i;
                    }
                }
                mTimeZone.setSelection(index);
            }
        }
    }

    private void configDateAndTime() {
        int auto_zone = Settings.Global.getInt(getActivity().getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 1);
        mTimeZone.setEnabled(auto_zone > 0 ? false : true);
        mAutoTimeZone.setChecked(auto_zone > 0 ? true : false);
        Logger.d(TAG, "configDateAndTime: ");
        mAutoTimeZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = ((Switch) v).isChecked();
                Settings.Global.putInt(getActivity().getContentResolver(), Settings.Global.AUTO_TIME_ZONE, isChecked ? 1 : 0);
                mTimeZone.setEnabled(!isChecked);
            }
        });

        mSwitch_12_24.setChecked(DateFormat.is24HourFormat(getContext()));
        mSwitch_12_24.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = ((Switch) v).isChecked();
                Settings.System.putString(getContext().getContentResolver(), Settings.System.TIME_12_24, isChecked ? "24" : "12");
                Intent timeChanged = new Intent(Intent.ACTION_TIME_CHANGED);
                timeChanged.putExtra(EXTRA_TIME_PREF_24_HOUR_FORMAT, isChecked);
                getContext().sendBroadcast(timeChanged);
            }
        });
        final String[][] timezones = getAllTimezones();
        TimeZone locale = TimeZone.getDefault();
        String localeID = locale.getID();
        String[] localeIDs = timezones[0];
        int index = 64;
        for (int i = 0; i < localeIDs.length; i++) {
            if (localeID.endsWith(localeIDs[i])) {
                index = i;
            }
        }
        mTimeZone.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, timezones[1]));
        mTimeZone.setSelection(index);
        Logger.d(TAG, "configDateAndTime:  locale = " + locale.getDisplayName() + ", localeID" + localeID);

        mTimeZone.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Logger.d(TAG, "onItemSelected: position = " + position);
                setTimeZone(timezones[0][position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //do nothing
            }
        });
    }

    public void setTimeZone(String localeID) {
        final AlarmManager alarm = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        alarm.setTimeZone(localeID);
    }

    private void configSystemFontSize() {
        String[] strEntryValues = getActivity().getResources().getStringArray(R.array.entry_values_font_size);
        final float textsize = Settings.System.getFloat(getContext().getContentResolver(), Settings.System.FONT_SCALE, 1.0f);
        Logger.d(TAG, "configSystemFontSize: textsize " + textsize);
        int index = 2;
        for (int i = 0; i < strEntryValues.length; i++) {
            if (Float.parseFloat(strEntryValues[i]) == textsize) {
                index = i;
                break;
            }
        }
        mTextSize.setSelection(index);
        mTextSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Logger.d(TAG, "configSystemFontSize: position " + position);
                String[] strEntryValues = getActivity().getResources().getStringArray(R.array.entry_values_font_size);
                Settings.System.putFloat(getContext().getContentResolver(), Settings.System.FONT_SCALE, Float.parseFloat(strEntryValues[position]));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //do nothing
            }
        });
    }

    private void configSystemLanguages() {
        String locale = Locale.getDefault().getCountry();
        Logger.d(TAG, "configSystemLanguages: locale " + locale);
        if ("CN".equals(locale)) {
            mSystemLanguages.setSelection(0);
        } else if ("US".equals(locale)) {
            mSystemLanguages.setSelection(1);
        }
        mSystemLanguages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] strEntryValues = getActivity().getResources().getStringArray(R.array.entries_values_system_languages);
                Logger.d(TAG, "onItemSelected: position = " + position + ", strEntryValues" + strEntryValues[position]);
                String[] selectLocale = strEntryValues[position].split("_");
                Locale newLocale = new Locale(selectLocale[0], selectLocale[1]);
                updateLocales(newLocale);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //do nothing
            }
        });
    }


    private void configBatteryPercentage() {
        int t = Settings.System.getInt(getContext().getContentResolver(), BATTERY_PERCENTAGE_ENABLE, 1);
        Logger.d(TAG, "configBatteryPercentage: t " + t);
        mBatteryPercentage.setChecked(t > 0 ? true : false);
        mBatteryPercentage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = ((Switch) v).isChecked();
                Settings.System.putInt(getContext().getContentResolver(), BATTERY_PERCENTAGE_ENABLE, isChecked ? 1 : 0);
            }
        });
    }


    private void configBrightnessLevel() {
        mBrightnessLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.d(TAG, "configBrightnessLevel onClick");
                Intent in = new Intent();
                in.setAction(INTENT);
                startActivity(in);
            }
        });
    }

    /*public static void updateLocales(LocaleList locales) {
        try {
             final IActivityManager am = ActivityManagerNative.getDefault();
             final Configuration config = am.getConfiguration();

             config.setLocales(locales);
             config.userSetLocale = true;

             am.updatePersistentConfiguration(config);
             // Trigger the dirty bit for the Settings Provider.
             BackupManager.dataChanged("com.android.providers.settings");
         } catch (RemoteException e) {
             // Intentionally left blank
         }
     }*/
    public static void updateLocales(Locale locales) {
        try {
            Object objIActMag, objActmagNative;
            Class clzIActMag = Class.forName("android.app.IActivityManager");
            Class clzActmagNative = Class.forName("android.app.ActivityManagerNative");
            // final IActivityManager am = ActivityManagerNative.getDefault();
            Method mtdActMagNative$getDefault = clzActmagNative.getDeclaredMethod("getDefault");
            objIActMag = mtdActMagNative$getDefault.invoke(clzActmagNative);

            //final Configuration config = am.getConfiguration();
            Method mtdIActMag$getConfiguration = clzIActMag.getDeclaredMethod("getConfiguration");
            Configuration config = (Configuration) mtdIActMag$getConfiguration.invoke(objIActMag);
            config.locale = locales;

            Class clzConfig = Class.forName("android.content.res.Configuration");
            Field userSetLocale = clzConfig.getField("userSetLocale");
            userSetLocale.set(config, true);
            Class[] clzParams = {Configuration.class};

            Method mtdIActMag$updateConfiguration = clzIActMag.getDeclaredMethod("updatePersistentConfiguration", clzParams);
            mtdIActMag$updateConfiguration.invoke(objIActMag, config);
            BackupManager.dataChanged("com.android.providers.settings");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String[][] getAllTimezones() {
        final Resources res = getActivity().getResources();
        final String[] ids = res.getStringArray(R.array.timezone_values);
        final String[] labels = res.getStringArray(R.array.timezone_labels);

        int minLength = ids.length;
        if (ids.length != labels.length) {
            minLength = Math.min(minLength, labels.length);
            Logger.d("Tag", "Timezone ids and labels have different length!");
        }

        final long currentTimeMillis = System.currentTimeMillis();
        final List<TimeZoneRow> timezones = new ArrayList<>(minLength);
        for (int i = 0; i < minLength; i++) {
            timezones.add(new TimeZoneRow(ids[i], labels[i], currentTimeMillis));
        }
        Collections.sort(timezones);

        final String[][] timeZones = new String[2][timezones.size()];
        int i = 0;
        for (TimeZoneRow row : timezones) {
            timeZones[0][i] = row.mId;
            timeZones[1][i++] = row.mDisplayName;
        }
        return timeZones;
    }

    private static class TimeZoneRow implements Comparable<TimeZoneRow> {

        private static final boolean SHOW_DAYLIGHT_SAVINGS_INDICATOR = false;

        public final String mId;
        public final String mDisplayName;
        public final int mOffset;

        public TimeZoneRow(String id, String name, long currentTimeMillis) {
            final TimeZone tz = TimeZone.getTimeZone(id);
            final boolean useDaylightTime = tz.useDaylightTime();
            mId = id;
            mOffset = tz.getOffset(currentTimeMillis);
            mDisplayName = buildGmtDisplayName(name, useDaylightTime);
        }

        @Override
        public int compareTo(TimeZoneRow another) {
            return mOffset - another.mOffset;
        }

        public String buildGmtDisplayName(String displayName, boolean useDaylightTime) {
            final int p = Math.abs(mOffset);
            final StringBuilder name = new StringBuilder("(GMT");
            name.append(mOffset < 0 ? '-' : '+');

            name.append(p / DateUtils.HOUR_IN_MILLIS);
            name.append(':');

            int min = p / 60000;
            min %= 60;

            if (min < 10) {
                name.append('0');
            }
            name.append(min);
            name.append(") ");
            name.append(displayName);
            if (useDaylightTime && SHOW_DAYLIGHT_SAVINGS_INDICATOR) {
                name.append(" \u2600"); // Sun symbol
            }
            return name.toString();
        }
    }
}
