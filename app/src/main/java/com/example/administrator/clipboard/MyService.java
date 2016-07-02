package com.example.administrator.clipboard;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.EditText;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyService extends Service {
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }

    private ClipboardManager clip;
    private ArrayList<Device> devices;
    private int connectPort = 8100;

    private HashMap<String, Integer> checkingQueue = new HashMap<String, Integer>();
    private ServerSocket s;
    private boolean isRunning = true;
    private String lastclip = "";

    public MyService() {}

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
            devices = intent.getParcelableArrayListExtra("devices");

            clip = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clip.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
                @Override
                public void onPrimaryClipChanged() {
                    ClipData cd = clip.getPrimaryClip();
                    if(!cd.getItemAt(0).getText().toString().equals(lastclip)) {
                        new Thread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        ClipData cd = clip.getPrimaryClip();
                                        System.out.println(cd.getItemAt(0).getText().toString());
                                        send(cd.getItemAt(0).getText().toString());
                                    }}).start();
                    }


                }
            });


            new Thread(new Runnable() {
                @Override
                public void run() {

                    initServer();
                }
            }).start();


        System.out.println("startujemy");

        connectWithAll();
        return Service.START_REDELIVER_INTENT;
    }


    public void send(String cb) {
        System.out.println("send " + cb);
        //for(ArrayList<Object> ip : devices) {
        for(int i = 0; i < devices.size(); i++) {
            System.out.println("try to " + devices.get(i).getName() + ", " + devices.get(i).getIP() +  "=" + getString(R.string.connected));

            if(devices.get(i).getStatus() == R.string.connected) {
                try {
                    System.out.println("Wysyłanie do " + devices.get(i).getName());
                    Socket s = new Socket(InetAddress.getByName(devices.get(i).getIP()), connectPort);
                    OutputStream outStream = s.getOutputStream();

                    PrintWriter out = new PrintWriter(outStream, true);
                    out.println(cb);

                    s.close();
                    outStream.close();
                }
                catch(ConnectException ce) {
                    changeStatus((String) devices.get(i).getIP(), i, R.string.disconnected);
                    //ce.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public void initServer() {

        try {
            if(s == null) s = new ServerSocket(8100);

            while(isRunning) {
                Socket incoming = s.accept();
                Runnable r = new GuestThread(this, incoming, clip);
                Thread t = new Thread(r);
                t.start();
            }

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

    }

    public int getPort() {
        return connectPort;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public void changePort(int port) {
        connectPort = port;
    }


    @Override
    public void onDestroy() {
        System.out.println("destroy");
       try {
            isRunning = false;
            s.close();
//
       } catch (IOException e) {
            e.printStackTrace();
       }
    }

    public void connectWithAll() {
        for(int i = 0; i < devices.size(); i++)
            connectWith(devices.get(i).getIP(), i);
    }

    public void connectWith(String ip2, int row2) {

        final String ip = ip2;
        final int row = row2;

        new Thread(
                new Runnable() {
                    public void run() {
                        System.out.println("connect " + ip);

                        changeStatus(ip, row, R.string.connecting);
                        checkConnection(ip, getString(R.string.CONN_CHECK), row);

                        checkingQueue.put(ip, row);

                    }
                }
        ).start();
    }

    public void doubleConnect(String ip) {
        System.out.println("2connect");
        for(int i = 0; i < devices.size(); i++)
            if(devices.get(i).getIP().equals(ip)) changeStatus(ip, i, R.string.connected);
    }

    public void changeStatus(String ip, int row, int status) {
        System.out.println("Zmiana statusu - " + status);

        devices.get(row).changeStatus(status);

        if(isForeground("com.example.administrator.clipboard")) {
            Intent i = new Intent("change-status");
            i.putExtra("row", row);
            i.putExtra("status", status);
            LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        }


        //wysyłanie do MainActiviti nowego statusu!!!!!!

        //if(status.equals(getString(R.string.connected))) model.setValueAt("Rozłącz", row, 3);
        //else model.setValueAt("Połącz", row, 3);
    }

    public void checkConnection(String ip, String msg, int row) {
        System.out.println("do wysłania - " + msg + " - " + ip + ":" + connectPort);

        try {
            Socket s = new Socket(ip, connectPort);
            OutputStream outStream = s.getOutputStream();

            PrintWriter out = new PrintWriter(outStream, true);
            out.println(msg);

            s.close();
            outStream.close();
        }
        catch(java.net.UnknownHostException e) {
            if(checkQueue(ip)) removeFromQueue(ip, false);
            else changeStatus(ip, row, R.string.disconnected);
        }
        catch(ConnectException ce) {
            if(checkQueue(ip)) removeFromQueue(ip, false);
            else changeStatus(ip, row, R.string.disconnected);
        }
        catch(IOException e) {
            e.printStackTrace();
        }

    }

    public boolean checkQueue(String ip) {
        return checkingQueue.containsKey(ip);
    }

    public boolean checkDevices(String ip) {
        for(Device a : devices)
            if(a.getIP().equals(ip)) return true;

        return false;
    }


    /*public void removeFromQueue(String ip) {
        System.out.println("usuwanie z kolejki");
        for(int i = 0; i < checkingQueue.size(); i++) {
            System.out.println(checkingQueue.get(i));
            if(checkingQueue.get(i).equals(ip)) {
                System.out.print(" - usunięto");
                checkingQueue.remove(i);
            }
        }
    }*/

    public void removeFromQueue(String ip, boolean flag) {
        if(flag) changeStatus(ip, checkingQueue.get(ip), R.string.connected);
        else changeStatus(ip, checkingQueue.get(ip), R.string.disconnected);

        checkingQueue.remove(ip);
    }

    public boolean isForeground(String myPackage) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        return componentInfo.getPackageName().equals(myPackage);
    }

    public void setLastclip(String s) {
        lastclip = s;
    }
}
