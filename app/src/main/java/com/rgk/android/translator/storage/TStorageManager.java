package com.rgk.android.translator.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.SparseArray;

import com.rgk.android.translator.AppContext;
import com.rgk.android.translator.database.TranslatorStorage;
import com.rgk.android.translator.database.beans.MemberBean;
import com.rgk.android.translator.database.beans.ThreadsBean;
import com.rgk.android.translator.database.beans.UserBean;
import com.rgk.android.translator.utils.Logger;

import java.util.HashMap;

public class TStorageManager {
    private static final String TAG = "RTranslator/TStorageManager";

    public static final String KEY_AUTO = "auto";
    public static final String KEY_TEXT_SIZE = "text_size";
    public static final String KEY_PAIR_DEVICE_ID = "PAIR_DEVICE_ID";
    public static final String KEY_STORAGE_TYPE = "key_storage_type";

    private Context mContext;
    //key: deviceId
    private HashMap<String, MemberBean> mMembers;
    //key: serverThreadId
    private HashMap<String, ThreadsBean> mThreads;

    private TranslatorStorage mTranslatorStorage;

    private static TStorageManager instance;

    private TStorageManager(Context context) {
        mContext = context;
        mTranslatorStorage = TranslatorStorage.getInstance();
        mThreads = mTranslatorStorage.getAllThreads();
//        mMembers = mTranslatorStorage.getAllMembers();
    }

    public static void init(Context context) {
        if (instance == null) {
            TranslatorStorage.init(context);
            instance = new TStorageManager(context);
        }
    }

    public static synchronized TStorageManager getInstance() {
        return instance;
    }

    public ThreadsBean getThreadBean(String serverThreadId) {
        return mThreads.get(serverThreadId);
    }

    public ThreadsBean createThread(String serverThreadId, long date) {
        ThreadsBean threadsBean = ThreadsBean.create(serverThreadId, date, 0, "", 0);
        mThreads.put(serverThreadId, threadsBean);
        return threadsBean;
    }

    public ThreadsBean createThread(String serverThreadId, long date, int messageCount,
                                    String title, int unreadCount) {
        ThreadsBean threadsBean = ThreadsBean.create(serverThreadId, date, messageCount, title, unreadCount);
        mThreads.put(serverThreadId, threadsBean);
        return threadsBean;
    }

    public void deleteThread(String serverThreadId) {
        Logger.v(TAG, "deleteThread- " + serverThreadId);
        mTranslatorStorage.delete(mThreads.get(serverThreadId));
        mThreads.remove(serverThreadId);
    }

    public void deleteThread(ThreadsBean thread) {
        Logger.v(TAG, "deleteThread: " + thread.getServerThreadId());
        mTranslatorStorage.delete(thread);
        mThreads.remove(thread.getServerThreadId());
    }

    public MemberBean getMember(String deviceId) {
        return mTranslatorStorage.getMemberByDeviceId(deviceId);
    }

    public MemberBean createMember(String deviceId, String language) {
        return MemberBean.create(deviceId, "", "", 0, 0, language, "", 0);
    }

    public MemberBean createMember(String deviceId, String name, String nickName,
                                   int sex, int photoId, String language, String description,
                                   int favorite) {
        return MemberBean.create(deviceId, name, nickName, sex, photoId, language, description, favorite);
    }

    public UserBean getUser() {
        return mTranslatorStorage.getUser();
    }

    public boolean isAuto() {
        SharedPreferences sp = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        return sp.getBoolean(KEY_AUTO, true);
    }

    public void setAuto(boolean b) {
        SharedPreferences sp = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        sp.edit().putBoolean(KEY_AUTO, b).commit();
    }

    public float getTextSize() {
        SharedPreferences sp = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        return sp.getFloat(KEY_TEXT_SIZE, AppContext.TEXT_SIZE_NORMAL);
    }

    public void setTextSize(float f) {
        SharedPreferences sp = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        sp.edit().putFloat(KEY_TEXT_SIZE, f).commit();
    }

    public String getPairedId() {
        SharedPreferences sp = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        String deviceId = sp.getString(KEY_PAIR_DEVICE_ID, "");
        return deviceId;
    }

    public void setPairedId(String id) {
        SharedPreferences sp = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        sp.edit().putString(KEY_PAIR_DEVICE_ID, id).commit();
    }

    /**
     *
     * @return storage type: 1-not save, 2-only save text, 3-only save sound, 4-both text and sound
     */
    public int getStorageType() {
        SharedPreferences sp = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        int type = sp.getInt(KEY_STORAGE_TYPE, 1);
        return type;
    }

    /**
     *
     * @param type 1: not save, 2: only save text, 3: only save sound, 4: both text and sound
     */
    public void setStorageType(int type) {
        SharedPreferences sp = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        sp.edit().putInt(KEY_STORAGE_TYPE, type).commit();
    }
}
