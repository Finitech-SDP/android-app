package uk.ac.ed.inf.finitech;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Button manualOverride = findViewById(R.id.manualButton);
        manualOverride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, ManualControlActivity.class);
                MainActivity.this.startActivity(myIntent);
            }
        });

        TextView ipAddress_tv = findViewById(R.id.ipAddress_tv);
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wm != null) {
            int ip = wm.getConnectionInfo().getIpAddress();
            if (ip != 0) {
                // TODO: deprecated API
                ipAddress_tv.setText(Formatter.formatIpAddress(ip));
            } else {
                ipAddress_tv.setText("Unknown (Is the device connected to Wi-Fi?)");
            }
        } else {
            ipAddress_tv.setText("N/A (Does the device have Wi-Fi support?)");
        }
    }

}
