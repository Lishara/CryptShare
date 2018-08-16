package com.example.app.crypyshare;

/**
 * Created by Rangana on 1/5/2018.
 */

public class RowItem {

    private String email;
    private int profile_pic_id;
    private String fileName;


    public RowItem(String email, int profile_pic_id, String fileName) {

        this.email = email;
        this.profile_pic_id = profile_pic_id;
        this.fileName = fileName;

    }

    public String getMember_name() {
        return email;
    }

    public void setMember_name(String email) {
        this.email = email;
    }

    public int getProfile_pic_id() {
        return profile_pic_id;
    }

    public void setProfile_pic_id(int profile_pic_id) {
        this.profile_pic_id = profile_pic_id;
    }

    public String getStatus() {
        return fileName;
    }

    public void setStatus(String fileName) {
        this.fileName = fileName;
    }
}
