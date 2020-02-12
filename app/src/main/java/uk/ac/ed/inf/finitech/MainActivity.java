package uk.ac.ed.inf.finitech;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;


public class MainActivity extends AppCompatActivity {
    private Button connectButton;
    static public TcpClient tcpClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        connectButton = findViewById(R.id.connectButton);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectButton.setEnabled(false);

                String serverIp = ((EditText) findViewById(R.id.serverIpEt)).getText().toString();
                int serverPort = Integer.parseInt(((EditText) findViewById(R.id.serverPortEt)).getText().toString());

                if (serverIp.equals("0.0.0.0")) {
                    tcpClient = null;
                    Intent myIntent = new Intent(MainActivity.this, ConnectRobotActivity.class);
                    MainActivity.this.startActivity(myIntent);
                    return;
                }

                tcpClient = new TcpClient(serverIp, serverPort);
                Thread tcpClientThread = new Thread(tcpClient);
                tcpClient.setEventHandler(new TcpClient.EventHandler() {
                    @Override
                    public void onConnect() {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent myIntent = new Intent(MainActivity.this, ConnectRobotActivity.class);
                                MainActivity.this.startActivity(myIntent);
                            }
                        });
                    }

                    @Override
                    public void onConnectError(Throwable tr) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                connectButton.setEnabled(true);
                                Snackbar.make(findViewById(android.R.id.content), "Connection Error", Snackbar.LENGTH_INDEFINITE).show();
                            }
                        });
                    }

                    @Override
                    public void onMessage(byte[] message) {
                    }

                    @Override
                    public void onOperationalError(Throwable tr) {
                    }
                });
                tcpClientThread.start();
            }
        });

        TextView ipAddress_tv = findViewById(R.id.ipAddress_tv);
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wm != null) {
            int ip = wm.getConnectionInfo().getIpAddress();
            if (ip != 0) {
                ipAddress_tv.setText(String.format(
                        "%d.%d.%d.%d",
                        (ip & 0xff),
                        (ip >> 8 & 0xff),
                        (ip >> 16 & 0xff),
                        (ip >> 24 & 0xff)));
            } else {
                ipAddress_tv.setText("Unknown (Is the device connected to Wi-Fi?)");
            }
        } else {
            ipAddress_tv.setText("N/A (Does the device have Wi-Fi support?)");
        }
    }
}
