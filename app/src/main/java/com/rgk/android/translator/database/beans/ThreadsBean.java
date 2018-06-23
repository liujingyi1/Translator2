package com.rgk.android.translator.database.beans;

import com.rgk.android.translator.database.TranslatorStorage;
import com.rgk.android.translator.utils.Logger;

import java.util.HashMap;
import java.util.List;

public class ThreadsBean {
    private static final String TAG = "RTranslator/ThreadsBean";

    private long id;
    private String serverThreadId;
    private long date;
    private int messageCount;
    private String title;
    private int unreadCount;
    private List<MessageBean> messageBeans;
    //Key: deviceId
    private HashMap<String, MemberBean> members;

    public ThreadsBean(long id) {
        this.id = id;
    }

    private ThreadsBean(String serverThreadId, long date, int messageCount,
                        String title, int unreadCount) {
        this.serverThreadId = serverThreadId;
        this.date =date;
        this.messageCount = messageCount;
        this.title = title;
        this.unreadCount = unreadCount;
        members = new HashMap<>();
        this.id = TranslatorStorage.getInstance().insert(this);
    }

    public static ThreadsBean create(String serverThreadId, long date, int messageCount,
                                     String title, int unreadCount) {
        return new ThreadsBean(serverThreadId, date, messageCount, title, unreadCount);
    }

    public boolean update(long date, int messageCount,
                          String title, int unreadCount) {
        this.date = date;
        this.messageCount = messageCount;
        this.title = title;
        this.unreadCount = unreadCount;
        TranslatorStorage.getInstance().update(this, false);
        return true;
    }

    public boolean delete() {
        int count = TranslatorStorage.getInstance().delete(this);
        return count > 0;
    }

    public long getId() {
        return id;
    }

    private void setId(long id) {
        this.id = id;
    }

    public String getServerThreadId() {
        return serverThreadId;
    }

    public void setServerThreadId(String serverThreadId) {
        this.serverThreadId = serverThreadId;
    }

    public void addMember(MemberBean memberBean) {
        if (members.containsKey(memberBean.getDeviceId())) {
            Logger.w(TAG, "Member exist - " + memberBean.getDeviceId());
        } else {
            members.put(memberBean.getDeviceId(), memberBean);
            TranslatorStorage.getInstance().update(this, true);
        }
    }

    public void removeMember(MemberBean memberBean) {
        removeMember(memberBean.getDeviceId());
    }

    public void removeMember(String deviceId) {
        members.remove(deviceId);
        TranslatorStorage.getInstance().update(this, true);
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isRead() {
        return unreadCount == 0;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public List<MessageBean> getMessageBeans() {
        return messageBeans;
    }

    public void setMessageBeans(List<MessageBean> messageBeans) {
        this.messageBeans = messageBeans;
    }

    public HashMap<String, MemberBean> getMembers() {
        return members;
    }

    public class Builder {
        private String serverThreadId;
        private long date;
        private int messageCount;
        private String title;
        private int unreadCount;

        public Builder(String serverThreadId) {
            this.serverThreadId = serverThreadId;
        }

        public Builder setDate(long date) {
            this.date = date;
            return this;
        }

        public Builder setMessageCount(int messageCount) {
            this.messageCount = messageCount;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setUnreadCount(int unreadCount) {
            this.unreadCount = unreadCount;
            return this;
        }

        public ThreadsBean build() {
            return new ThreadsBean(serverThreadId, date, messageCount,
                    title, unreadCount);
        }
    }
}
