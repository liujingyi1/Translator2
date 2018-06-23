package com.rgk.android.translator.database.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.rgk.android.translator.database.DbConstants;
import com.rgk.android.translator.database.DatabaseHelper;
import com.rgk.android.translator.database.ThreadsDao;
import com.rgk.android.translator.database.beans.MemberBean;
import com.rgk.android.translator.database.beans.ThreadsBean;
import com.rgk.android.translator.utils.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ThreadsDaoImpl implements ThreadsDao {
    private static final String TAG = "RTranslator/ThreadsDaoImpl";
    private Context mContext;
    private DatabaseHelper mDatabaseHelper;

    public ThreadsDaoImpl(Context context, DatabaseHelper databaseHelper) {
        mContext = context;
        mDatabaseHelper = databaseHelper;
    }
    @Override
    public HashMap<String, ThreadsBean> getAllThreads() {
        String sql = "SELECT * FROM threads;";
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor == null) {
            return null;
        }
        HashMap<String, ThreadsBean> threadsBeanHashMap = new HashMap<>();
        while (cursor.moveToNext()) {
            ThreadsBean threadsBean = getThreadsBean(cursor);
            threadsBeanHashMap.put(threadsBean.getServerThreadId(), threadsBean);
        }
        cursor.close();
        return threadsBeanHashMap;
    }

    @Override
    public long insert(ThreadsBean threadsBean) {
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        ContentValues values = buildContentValues(threadsBean);
        long id = database.insert(DbConstants.Tables.TABLE_THREADS,
                DbConstants.ThreadsColumns.TITLE, values);
        return id;
    }

    @Override
    public int update(ThreadsBean threadsBean, boolean onlyUpdateMember) {
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        String where = "_id="+threadsBean.getId();
        if (onlyUpdateMember) {
            ContentValues values = new ContentValues();
            HashMap<String, MemberBean> map = threadsBean.getMembers();
            Iterator<Map.Entry<String, MemberBean>> iterator = map.entrySet().iterator();
            StringBuilder memberIds = new StringBuilder();
            while (iterator.hasNext()) {
                Map.Entry<String, MemberBean> entry = iterator.next();
                MemberBean memberBean = entry.getValue();
                memberIds.append(memberBean.getId());
                memberIds.append(",");
            }
            if (memberIds.length() > 0) {
                memberIds.deleteCharAt(memberIds.length() - 1);
                values.put(DbConstants.ThreadsColumns.MEMBER_ID, memberIds.toString());
            } else {
                Logger.d(TAG, "To clear member.");
                values.put(DbConstants.ThreadsColumns.MEMBER_ID, "");
            }
            return database.update(DbConstants.Tables.TABLE_THREADS, values, where, null);
        } else {
            ContentValues values = buildContentValues(threadsBean);
            return database.update(DbConstants.Tables.TABLE_THREADS, values, where, null);
        }
    }

    private ContentValues buildContentValues(ThreadsBean threadsBean) {
        ContentValues values = new ContentValues();
        if (!TextUtils.isEmpty(threadsBean.getServerThreadId())) {
            values.put(DbConstants.ThreadsColumns.SERVER_THREAD_ID, threadsBean.getServerThreadId());
        }
        if (threadsBean.getDate() > 0) {
            values.put(DbConstants.ThreadsColumns.DATE, threadsBean.getDate());
        }
        if (threadsBean.getMessageCount() > 0) {
            values.put(DbConstants.ThreadsColumns.MESSAGE_COUNT, threadsBean.getMessageCount());
        }
        if (!TextUtils.isEmpty(threadsBean.getTitle())) {
            values.put(DbConstants.ThreadsColumns.TITLE, threadsBean.getTitle());
        }
        return values;
    }

    @NonNull
    private ThreadsBean getThreadsBean(Cursor cursor) {
        long id = cursor.getLong(
                cursor.getColumnIndexOrThrow(DbConstants.ThreadsColumns.ID));
        ThreadsBean threadsBean = new ThreadsBean(id);
        threadsBean.setServerThreadId(cursor.getString(
                cursor.getColumnIndexOrThrow(DbConstants.ThreadsColumns.SERVER_THREAD_ID)));
        threadsBean.setDate(cursor.getLong(
                cursor.getColumnIndexOrThrow(DbConstants.ThreadsColumns.DATE)));
        threadsBean.setMessageCount(cursor.getInt(
                cursor.getColumnIndexOrThrow(DbConstants.ThreadsColumns.MESSAGE_COUNT)));
        threadsBean.setTitle(cursor.getString(
                cursor.getColumnIndexOrThrow(DbConstants.ThreadsColumns.TITLE)));
        threadsBean.setUnreadCount(cursor.getInt(
                cursor.getColumnIndexOrThrow(DbConstants.ThreadsColumns.UNREAD_COUNT)));

        String memberId = cursor.getString(
                cursor.getColumnIndexOrThrow(DbConstants.ThreadsColumns.MEMBER_ID));
        String where = "(" + memberId + ")";
        HashMap<String, MemberBean> members = queryMembers(where);
        threadsBean.getMembers().putAll(members);

        return threadsBean;
    }

    private HashMap<String, MemberBean> queryMembers(String where) {
        String sql = "SELECT * FROM member WHERE _id IN " + where + ";";
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery(sql, null, null);
        HashMap<String, MemberBean> members = new HashMap<>();
        if (cursor == null) {
            return members;
        }
        while (cursor.moveToNext()) {
            String deviceId = cursor.getString(
                    cursor.getColumnIndexOrThrow(DbConstants.MemberColumns.DEVICE_ID));
            MemberBean memberBean = getMember(cursor, deviceId);
            members.put(deviceId, memberBean);
        }
        cursor.close();
        return members;
    }

    private MemberBean getMember(Cursor cursor, String deviceId) {
        MemberBean memberBean = new MemberBean(cursor.getLong(
                cursor.getColumnIndexOrThrow(DbConstants.MemberColumns.ID)));
        memberBean.setDeviceId(deviceId);
        memberBean.setName(cursor.getString(
                cursor.getColumnIndexOrThrow(DbConstants.MemberColumns.NAME)));
        memberBean.setNickName(cursor.getString(
                cursor.getColumnIndexOrThrow(DbConstants.MemberColumns.NICK_NAME)));
        memberBean.setSex(cursor.getInt(
                cursor.getColumnIndexOrThrow(DbConstants.MemberColumns.SEX)));
        memberBean.setPhotoId(cursor.getInt(
                cursor.getColumnIndexOrThrow(DbConstants.MemberColumns.PHOTO_ID)));
        memberBean.setLanguage(cursor.getString(
                cursor.getColumnIndexOrThrow(DbConstants.MemberColumns.LANGUAGE)));
        memberBean.setDescription(cursor.getString(
                cursor.getColumnIndexOrThrow(DbConstants.MemberColumns.DESCRIPTION)));
        memberBean.setFavorite(cursor.getInt(
                cursor.getColumnIndexOrThrow(DbConstants.MemberColumns.FAVORITE)));
        return memberBean;
    }

    @Override
    public int delete(ThreadsBean threadsBean) {
        if (threadsBean == null) {
            return 0;
        }
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        String where = DbConstants.ThreadsColumns.ID + "=" + threadsBean.getId();
        return database.delete(DbConstants.Tables.TABLE_THREADS, where, null);
    }

    @Override
    public int delete(List<ThreadsBean> threadsBeans) {
        if (threadsBeans == null || threadsBeans.size() == 0) {
            return 0;
        }
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        String where = DbConstants.ThreadsColumns.ID + " IN (";
        StringBuilder builder = new StringBuilder();
        builder.append("DELETE FROM threads WHERE ");
        builder.append(where);
        for (ThreadsBean threadsBean : threadsBeans) {
            builder.append(threadsBean.getId());
            builder.append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append(");");
        database.execSQL(builder.toString());
        return threadsBeans.size();
    }

    @Override
    public int deleteAllThreads() {
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        return database.delete(DbConstants.Tables.TABLE_THREADS, null, null);
    }
}
