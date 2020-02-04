package uk.ac.ed.inf.finitech;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {
    static public TcpClient tcpClient;

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
                String serverIp = ((EditText) findViewById(R.id.serverIpEt)).getText().toString();
                int serverPort = Integer.parseInt(((EditText) findViewById(R.id.serverPortEt)).getText().toString());

                tcpClient = new TcpClient(serverIp, serverPort);
                Thread tcpClientThread = new Thread(tcpClient);
                tcpClient.setEventHandler(new TcpClient.EventHandler() {
                    @Override
                    public void onConnect() {
                        Intent myIntent = new Intent(MainActivity.this, ConnectRobotActivity.class);
                        MainActivity.this.startActivity(myIntent);
                    }

                    @Override
                    public void onConnectError(Throwable tr) {
                        Snackbar.make(findViewById(android.R.id.content), "Connection Error", Snackbar.LENGTH_INDEFINITE).show();
                    }

                    @Override
                    public void onMessage(byte[] message) { }

                    @Override
                    public void onOperationalError(Throwable tr) {}
                });
                tcpClientThread.start();
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
