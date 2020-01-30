package uk.ac.ed.inf.finitech;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button connectButton = findViewById(R.id.connectButton);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String server_ip = ((EditText) findViewById(R.id.serverIpEt)).getText().toString();
                int server_port = Integer.parseInt(((EditText) findViewById(R.id.serverPortEt)).getText().toString());

                Intent myIntent = new Intent(MainActivity.this, ManualControlActivity.class);
                myIntent.putExtra("serverIp", server_ip);
                myIntent.putExtra("serverPort", server_port);
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
