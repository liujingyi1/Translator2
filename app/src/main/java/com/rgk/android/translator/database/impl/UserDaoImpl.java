package com.rgk.android.translator.database.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.rgk.android.translator.database.DbConstants;
import com.rgk.android.translator.database.DatabaseHelper;
import com.rgk.android.translator.database.UserDao;
import com.rgk.android.translator.database.beans.UserBean;
import com.rgk.android.translator.utils.Logger;

public class UserDaoImpl implements UserDao {
    private Context mContext;
    private DatabaseHelper mDatabaseHelper;
    private UserBean mUserBean;

    public UserDaoImpl(Context context, DatabaseHelper databaseHelper) {
        mContext = context;
        mDatabaseHelper = databaseHelper;
        mUserBean = getUser();
        if (mUserBean == null) {
            initUser();
        }
    }

    private String getDeviceId() {
        String deviceId = Build.SERIAL;
        if (TextUtils.isEmpty(deviceId)) {
            String time = Long.toString((System.currentTimeMillis() / (1000 * 60 * 60)));
            deviceId = time + time;
        }
        return deviceId;
    }

    @Override
    public UserBean getUser() {
        if (mUserBean != null) {
            return mUserBean;
        }
        String sql = "SELECT * FROM user;";
        return query(sql, null);
    }

    @Override
    public UserBean getUserById(int id) {
        String sql = "SELECT * FROM user WHERE _id = ?;";
        return query(sql, new String[]{String.valueOf(id)});
    }

    private UserBean query(String sql, String[] selectionArgs) {
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery(sql, selectionArgs);
        if (cursor == null) {
            return null;
        }
        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        UserBean userBean = new UserBean();
        userBean.setId(cursor.getInt(
                cursor.getColumnIndexOrThrow(DbConstants.UserColumns.ID)));
        userBean.setDeviceId(cursor.getString(
                cursor.getColumnIndexOrThrow(DbConstants.UserColumns.DEVICE_ID)));
        userBean.setRole(cursor.getString(
                cursor.getColumnIndexOrThrow(DbConstants.UserColumns.ROLE)));
        userBean.setName(cursor.getString(
                cursor.getColumnIndexOrThrow(DbConstants.UserColumns.NAME)));
        userBean.setNickName(cursor.getString(
                cursor.getColumnIndexOrThrow(DbConstants.UserColumns.NICK_NAME)));
        userBean.setSex(cursor.getInt(
                cursor.getColumnIndexOrThrow(DbConstants.UserColumns.SEX)));
        userBean.setPhotoId(cursor.getInt(
                cursor.getColumnIndexOrThrow(DbConstants.UserColumns.PHOTO_ID)));
        userBean.setLanguage(cursor.getString(
                cursor.getColumnIndexOrThrow(DbConstants.UserColumns.LANGUAGE)));
        userBean.setDescription(cursor.getString(
                cursor.getColumnIndexOrThrow(DbConstants.UserColumns.DESCRIPTION)));
        cursor.close();
        return userBean;
    }

    private void initUser() {
        Logger.d("RTranslator/UserDaoImpl", "initUser()");
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        String deviceId = getDeviceId();
        String sql = "INSERT INTO user(device_id, role, language) " +
                "VALUES('" + deviceId + "', 'admin', 'en-US');";
        database.execSQL(sql);

        mUserBean = new UserBean();
        mUserBean.setId(1);
        mUserBean.setDeviceId(deviceId);
        mUserBean.setRole("admin");
        mUserBean.setLanguage("en-US");
    }

    @NonNull
    private ContentValues buildContentValues(UserBean userBean) {
        ContentValues values = new ContentValues();
        if (TextUtils.isEmpty(userBean.getDeviceId())) {
            values.put(DbConstants.UserColumns.DEVICE_ID, getDeviceId());
        }
        if (userBean.getRole() != null) {
            values.put(DbConstants.UserColumns.ROLE, userBean.getRole());
        }
        if (userBean.getName() != null) {
            values.put(DbConstants.UserColumns.NAME, userBean.getName());
        }
        if (userBean.getNickName() != null) {
            values.put(DbConstants.UserColumns.NICK_NAME, userBean.getNickName());
        }
        if (userBean.getSex() > 0) {
            values.put(DbConstants.UserColumns.SEX, userBean.getSex());
        }
        if (userBean.getPhotoId() > 0) {
            values.put(DbConstants.UserColumns.PHOTO_ID, userBean.getPhotoId());
        }
        if (userBean.getLanguage() != null) {
            values.put(DbConstants.UserColumns.LANGUAGE, userBean.getLanguage());
        }
        if (userBean.getDescription() != null) {
            values.put(DbConstants.UserColumns.DESCRIPTION, userBean.getDescription());
        }
        return values;
    }

    @Override
    public int update(UserBean userBean) {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        ContentValues values = buildContentValues(userBean);
        mUserBean = userBean;
        return database.update(DbConstants.Tables.TABLE_USER, values,
                "_id=" + userBean.getId(), null);
    }
}
