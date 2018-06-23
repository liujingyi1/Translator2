package com.rgk.android.translator.database;

import com.rgk.android.translator.database.beans.ThreadsBean;

import java.util.HashMap;
import java.util.List;

public interface ThreadsDao {
    HashMap<String, ThreadsBean> getAllThreads();
    long insert(ThreadsBean threadsBean);
    int update(ThreadsBean threadsBean, boolean onlyUpdateMember);
    int delete(ThreadsBean threadsBean);
    int delete(List<ThreadsBean> threadsBeans);
    int deleteAllThreads();
}
