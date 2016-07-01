package com.example.administrator.clipboard;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private MainActivity ma = this;
    private MyService ms;
    private Intent intent = null;

    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private BroadcastReceiver receiver, receiver2;

    ArrayList<Device> devices = new ArrayList<Device>();
    ArrayList<DeviceView> devicesview = new ArrayList<DeviceView>();
    String autostart = "0";
    int connectPort = 8100;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            MyService.LocalBinder binder = (MyService.LocalBinder) service;
            ms = binder.getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            ms = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.icon);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        setContentView(R.layout.activity_main);

        final ToggleButton serviceToggle = (ToggleButton) findViewById(R.id.serviceToggle);
        serviceToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Działa");

                if(serviceToggle.isChecked()) {
                    /*NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(ma)
                                    .setSmallIcon(R.drawable.icon)
                                    .setContentTitle("Clipboard+")
                                    .setContentText("Service działa");


                    NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    mNotifyMgr.notify(001, mBuilder.build());*/

                    if(intent == null) {
                        System.out.println("intent");
                        intent = new Intent(ma, MyService.class);

                        receiver2= new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                Bundle extras = intent.getExtras();
                                devicesview.get(extras.getInt("row")).changeStatus(extras.getInt("status"));
                            }
                        };


                        System.out.println("jazda");
                        LocalBroadcastManager.getInstance(ma).registerReceiver(receiver2, new IntentFilter("change-status"));
                        for(Device d : devices) {
                            System.out.println("m - " + d.getIP());
                        }
                        intent.putParcelableArrayListExtra("devices", devices);
                        bindService(intent, mConnection, BIND_AUTO_CREATE);
                    }

                    startService(intent);
                }
                else {
                    System.out.println("stop service");
                    stopService(intent);
                    intent = null;

                    for(DeviceView dv :  devicesview) dv.changeStatus(R.string.disconnected);
                }

            }
        });

        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        try {
            //JSONObject json = new JSONObject("{\"settings\": {\"port\" : \"8100\",\"autostart\" : \"1\"},\"devices\" : [{\"name\" : \"KSANORK-MACHINE\",\"ip\" : " + "\"192.168.0.2\"},{\"name\" : \"KSANORK-LITE\",\"ip\" : \"192.168.0.4\"}]}");

            String jsons = settings.getString("JSONString", null);
            if(jsons != null) {
                JSONObject json = new JSONObject(settings.getString("JSONString", null));
                JSONArray jsondevices = json.optJSONArray("devices");

                System.out.println("Port - " + json.optString("port"));
                System.out.println("Port - " + json.optString("autostart"));

                if(jsondevices != null) {
                    LinearLayout ll = (LinearLayout) findViewById(R.id.content);

                    for(int i = 0; i < jsondevices.length(); i++) {
                        JSONObject device = jsondevices.getJSONObject(i);
                        Device d = new Device(device.optString("name"), device.optString("ip"));
                        DeviceView dv = new DeviceView(this, d);
                        registerForContextMenu(dv);
                        devices.add(d);
                        devicesview.add(dv);
                        ll.addView(dv);
                        //Log.d("json", device.optString("name") + " - " + device.optString("ip"));
                    }
                }
            }

        }
        catch (JSONException e) {
            e.printStackTrace();
        }


        if(isServiceRunning(MyService.class)) {
            serviceToggle.setChecked(true);
            intent = new Intent(ma, MyService.class);
            bindService(intent, mConnection, BIND_AUTO_CREATE);
            stopService(intent);

            System.out.println("intent");
            unbindService(mConnection);
            intent = new Intent(ma, MyService.class);

            receiver2= new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Bundle extras = intent.getExtras();
                    devicesview.get(extras.getInt("row")).changeStatus(extras.getInt("status"));
                }
            };


            System.out.println("jazda");
            LocalBroadcastManager.getInstance(ma).registerReceiver(receiver2, new IntentFilter("change-status"));
            for(Device d : devices) {
                System.out.println("m - " + d.getIP());
            }
            intent.putParcelableArrayListExtra("devices", devices);
            bindService(intent, mConnection, BIND_AUTO_CREATE);

            //intent = new Intent(ma, MyService.class);
            //bindService(intent, mConnection, BIND_AUTO_CREATE);
            startService(intent);

        }
        else serviceToggle.setChecked(false);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.add_device) {
            Intent intent = new Intent(this, AddDevice.class);
            startActivity(intent);

            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    System.out.println("Otrzymano zwrot!");
                    Bundle extras = intent.getExtras();
                    addDevice(extras.getString("name"), extras.getString("ip"));
                    saveJSON(getJSON());
                }
            };

            LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("add-device"));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        //menu.setHeaderTitle("Context Menu");
        menu.add(0, v.getId(), 0, "Rozłącz");
        menu.add(0, v.getId(), 0, "Usuń");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(intent != null) unbindService(mConnection);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver2);
    }

    public void addDevice(String name, String ip) {
        LinearLayout ll = (LinearLayout) findViewById(R.id.content);
        Device d = new Device(name, ip);
        DeviceView dv = new DeviceView(this, d);

        devices.add(d);
        devicesview.add(dv);
        ll.addView(dv);

        saveJSON(getJSON());
    }

    public void clearJSON() {
        editor = settings.edit();
        editor.clear();
        editor.commit();
    }

    //zapisywanie json w data
    public void saveJSON(JSONObject json) {
        editor = settings.edit();
        editor.clear();
        editor.putString("JSONString", json.toString());
        editor.commit();
    }


    //przekształcanie tablicy w json
    public JSONObject getJSON() {
        JSONObject json = new JSONObject();

        try {
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < devices.size(); i++) {
                JSONObject device = new JSONObject();
                device.put("name", (String) devices.get(i).getName());
                device.put("ip", (String) devices.get(i).getIP());
                jsonArray.put(device);
            }

            if(intent != null) connectPort = ms.getPort();

            json.put("port", connectPort);
            json.put("autostart", autostart);
            json.put("devices", jsonArray);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return json;
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
