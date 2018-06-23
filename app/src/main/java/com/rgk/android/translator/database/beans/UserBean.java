package com.rgk.android.translator.database.beans;

import com.rgk.android.translator.database.TranslatorStorage;

public class UserBean {
    private long id;
    private String deviceId;
    private String name;
    private String nickName;
    private int sex;
    private int photoId;
    private String language;
    private String description;
    private String role;

    public static UserBean getUser() {
        return TranslatorStorage.getInstance().getUser();
    }

    public boolean update(String name, String nickName, int sex,
                          int photoId, String language, String description,
                          String role) {
        this.name = name;
        this.nickName = nickName;
        this.sex = sex;
        this.photoId = photoId;
        this.language = language;
        this.description = description;
        this.role = role;
        int count = TranslatorStorage.getInstance().update(this);
        return count > 0;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getPhotoId() {
        return photoId;
    }

    public void setPhotoId(int photoId) {
        this.photoId = photoId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int convertToResId(int photoId) {
        return photoId;
    }

    public int convertToId(int photoResId) {
        return photoResId;
    }
}
