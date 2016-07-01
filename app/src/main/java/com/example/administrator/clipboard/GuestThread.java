package com.example.administrator.clipboard;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Created by Administrator on 2015-08-20.
 */
public class GuestThread implements Runnable {

    private Socket incoming;
    private MyService ma;
    private ClipboardManager clipboard;
    private ClipboardManager clipboard2;

    GuestThread(MyService ma, Socket incoming, ClipboardManager clip) {
        this.incoming = incoming;
        this.ma = ma;
        clipboard = clip;
    }

    public void run() {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(incoming.getInputStream()));

            String line = "", read = "";
            int i = 0;
            while((line = input.readLine()) != null) {
                if(i++ > 0) read += "\r\n";
                read += line;
            }


            StringTokenizer st = new StringTokenizer(incoming.getRemoteSocketAddress().toString(), ":/");
            String ip = st.nextToken();


            if(read.equals(ma.getString(R.string.CONN_CHECK))) {
                if(ma.checkDevices(ip)) {
                    ma.checkConnection(ip, ma.getString(R.string.CONN_OK), -1);
                    ma.doubleConnect(ip);
                }
                else {
                    System.out.println("Odrzucono");
                    ma.checkConnection(ip, ma.getString(R.string.CONN_REJECTED), -1);
                }
            }
            else if(read.equals(ma.getString(R.string.CONN_OK))) {
                if(ma.checkQueue(ip)) {
                    System.out.println("wszytsko ok");
                    ma.removeFromQueue(ip, true);
                }
            }
            else if(read.equals(ma.getString(R.string.CONN_REJECTED))) {
                ma.removeFromQueue(ip, false);
            }
            else {
                ClipData clip = ClipData.newPlainText("1", read);
                ma.setLastclip(read);
                ClipboardManager cm = (ClipboardManager) ma.getSystemService(ma.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(clip);

                //powidaomienie
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(ma)
                                .setSmallIcon(R.drawable.icon)
                                .setContentTitle("Clipboard+")
                                .setContentText(read);


                NotificationManager mNotifyMgr = (NotificationManager) ma.getSystemService(ma.NOTIFICATION_SERVICE);
                mNotifyMgr.notify(001, mBuilder.build());
                ///
            }




            incoming.close();
            //s.close();
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }

}

