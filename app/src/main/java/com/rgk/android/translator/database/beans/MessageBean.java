package com.rgk.android.translator.database.beans;

import com.rgk.android.translator.database.TranslatorStorage;

public class MessageBean {
    private long id;
    private long threadId;
    private long memberId = -1;
    private long date;
    private int read = 0;
    private int type;
    private String text;
    private String url;
    private int errorCode;
    private String language;
    private String serverThreadId;
    private String deviceId;

    public MessageBean(long id) {
        this.id = id;
    }

    private MessageBean(long threadId, long memberId, long date,
                        int read, int type, String text, String url,
                        int errorCode, String language,
                        String serverThreadId, String deviceId) {
        this.threadId = threadId;
        this.memberId = memberId;
        this.date = date;
        this.read = read;
        this.type = type;
        this.text = text;
        this.url = url;
        this.errorCode = errorCode;
        this.language = language;
        this.serverThreadId = serverThreadId;
        this.deviceId = deviceId;
        this.id = TranslatorStorage.getInstance().insert(this);
    }

    public static MessageBean create(long threadId, long memberId, long date,
                                     int read, int type, String text, String url,
                                     int errorCode, String language) {
        return new MessageBean(threadId, memberId, date, read, type,
                text, url, errorCode, language, null, null);
    }

    public boolean update(int read, int type, String text, String url,
                          int errorCode, String language) {
        // This is useful for draft,it can support edit.
        this.read = read;
        this.type = type;
        this.text = text;
        this.url = url;
        this.errorCode = errorCode;
        this.language = language;
        int count = TranslatorStorage.getInstance().update(this);
        return count > 0;
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

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public long getMemberId() {
        return memberId;
    }

    public void setMemberId(long memberId) {
        this.memberId = memberId;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getRead() {
        return read;
    }

    /**
     *
     * @param read unread:0, read:1
     */
    public void setRead(int read) {
        this.read = read;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public boolean isSend() {
        return getMemberId() == -1;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getServerThreadId() {
        return serverThreadId;
    }

    public void setServerThreadId(String serverThreadId) {
        this.serverThreadId = serverThreadId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public class Builder {
        private long threadId;
        private long memberId = -1;
        private long date;
        private int read = 0;
        private int type;
        private String text;
        private String url;
        private int errorCode;
        private String language;
        private String serverThreadId;
        private String deviceId;

        public Builder(long threadId, long memberId) {
            this.threadId = threadId;
            this.memberId = memberId;
        }

        public Builder setDate(long date) {
            this.date = date;
            return this;
        }

        /**
         *
         * @param read unread:0, read:1
         * @return
         */
        public Builder setRead(int read) {
            this.read = read;
            return this;
        }

        public Builder setType(int type) {
            this.type = type;
            return this;
        }

        public Builder setText(String text) {
            this.text = text;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setErrorCode(int errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder setLanguage(String language) {
            this.language = language;
            return this;
        }

        public Builder setServerThreadId(String serverThreadId) {
            this.serverThreadId = serverThreadId;
            return this;
        }

        public Builder setDeviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public MessageBean build() {
            return new MessageBean(threadId, memberId, date,
            read, type, text, url, errorCode, language,
            serverThreadId, deviceId);
        }
    }
}
