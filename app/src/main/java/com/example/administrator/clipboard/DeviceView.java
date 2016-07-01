package com.example.administrator.clipboard;

import android.content.Context;
import android.graphics.Color;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Administrator on 2015-09-17.
 */
public class DeviceView extends LinearLayout {

    private TextView textConnect;

    DeviceView(Context c, Device d) {
        super(c);

        setPadding(36, 36, 16, 36);

        setOrientation(LinearLayout.VERTICAL);
        setClickable(true);
        setBackgroundResource(R.drawable.background);

        TextView textName = new TextView(c);
        textName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        textName.setText(d.getName());
        //textCel.setTypeface(null, Typeface.BOLD);
        textName.setTextSize(18);
        addView(textName);

        TextView textIP = new TextView(c);
        textIP.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        textIP.setText(d.getIP());
        textIP.setTextSize(16);
        addView(textIP);

        textConnect = new TextView(c);
        textConnect.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        textConnect.setText("Rozłączony");
        textConnect.setTextColor(Color.RED);
        textConnect.setTextSize(16);
        addView(textConnect);
    }


    public void changeStatus(int status) {
        switch(status) {
            case R.string.connecting:
                textConnect.setText(R.string.connecting);
                textConnect.setTextColor(Color.BLACK);
                break;
            case R.string.connected:
                textConnect.setText(R.string.connected);
                textConnect.setTextColor(Color.parseColor("#8bc34a"));
                break;
            default:
                textConnect.setText(R.string.disconnected);
                textConnect.setTextColor(Color.RED);

        }
    }

    public int getStatus() {
        if(textConnect.getText().equals(R.string.connecting)) return R.string.connecting;
        else if(textConnect.getText().equals(R.string.connected)) return R.string.connected;
        else return R.string.disconnected;
    }
}