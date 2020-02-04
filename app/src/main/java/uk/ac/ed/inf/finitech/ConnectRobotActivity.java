package uk.ac.ed.inf.finitech;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class ConnectRobotActivity extends AppCompatActivity {
    private final String TAG = "ConnectRobotActivity";
    TcpClient tcpClient;
    private Button connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_robot);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        connectButton = findViewById(R.id.connectButton);

        tcpClient = MainActivity.tcpClient;
        tcpClient.setEventHandler(new TcpClient.EventHandler() {
            @Override
            public void onConnect() { }

            @Override
            public void onConnectError(Throwable tr) { }

            @Override
            public void onMessage(final byte[] message) {
                ConnectRobotActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ConnectRobotActivity.this.onTcpMessage(message);
                    }
                });
            }

            @Override
            public void onOperationalError(final Throwable tr) {
                ConnectRobotActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ConnectRobotActivity.this.onTcpError(tr);
                    }
                });
            }
        });
        tcpClient.startOperating();

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectButton.setEnabled(false);

                String robotIp = ((EditText) findViewById(R.id.robotIpEt)).getText().toString();
                int robotPort = Integer.parseInt(((EditText) findViewById(R.id.robotPortEt)).getText().toString());

                Snackbar.make(findViewById(android.R.id.content), "Connecting...", Snackbar.LENGTH_INDEFINITE).show();
                tcpClient.sendMessage(String.format("CONNECT %s %d", robotIp, robotPort).getBytes());
            }
        });
    }

    private void onTcpMessage(byte[] message) {
        try {
            Log.i(TAG, String.format("Received: %s|<<<", new String(message, "ascii")));
        } catch (UnsupportedEncodingException exc) {
            Log.i(TAG, String.format("Received: %s", Arrays.toString(message)));
        }

        if (Arrays.equals(message, "CONNECTED".getBytes())) {
            Intent myIntent = new Intent(ConnectRobotActivity.this, ManualControlActivity.class);
            ConnectRobotActivity.this.startActivity(myIntent);
        } else if (Arrays.equals(message, "ERROR".getBytes())) {
            Snackbar.make(findViewById(android.R.id.content), "Could not connect, try again.", Snackbar.LENGTH_INDEFINITE).show();
            connectButton.setEnabled(true);
        }
    }

    private void onTcpError(Throwable tr) {
        Snackbar.make(findViewById(android.R.id.content), "Operational Error", Snackbar.LENGTH_INDEFINITE).show();
    }

}
