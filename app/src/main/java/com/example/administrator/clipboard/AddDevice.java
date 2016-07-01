package com.example.administrator.clipboard;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddDevice extends AppCompatActivity {

    private AddDevice ad = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        setTitle("Dodaj urządzenie");

        ((Button) findViewById(R.id.buttonAdd)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = ((EditText) findViewById(R.id.editName)).getText().toString();
                String ip = ((EditText) findViewById(R.id.editIP)).getText().toString();

                Intent i = new Intent("add-device");
                i.putExtra("name", name);
                i.putExtra("ip", ip);
                LocalBroadcastManager.getInstance(ad).sendBroadcast(i);

                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_device, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
