package com.example.administrator.clipboard;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Administrator on 2015-09-15.
 */
public class Device implements Parcelable {

    private String ip, name;
    private int status;

    Device(String name, String ip) {
        this.ip = ip;
        this.name = name;
    }


    protected Device(Parcel in) {
        ip = in.readString();
        name = in.readString();
        status = in.readInt();
    }

    public static final Creator<Device> CREATOR = new Creator<Device>() {
        @Override
        public Device createFromParcel(Parcel in) {
            return new Device(in);
        }

        @Override
        public Device[] newArray(int size) {
            return new Device[size];
        }
    };

    public void changeStatus(int s) {
        status = s;
    }

    public int getStatus() {
        return status;
    }

    public String getIP() {
        return ip;
    }

    public String getName() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ip);
        dest.writeString(name);
        dest.writeInt(status);
    }
}
