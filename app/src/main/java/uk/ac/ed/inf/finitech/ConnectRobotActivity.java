package uk.ac.ed.inf.finitech;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ConnectRobotActivity extends AppCompatActivity {
    TcpClient tcpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_robot);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tcpClient = MainActivity.tcpClient;
        tcpClient.setEventHandler(new TcpClient.EventHandler() {
            @Override
            public void onMessage(byte[] message) {
                ConnectRobotActivity.this.onTcpMessage(message);
            }

            @Override
            public void onError(Throwable tr) {
                ConnectRobotActivity.this.onTcpError(tr);
            }
        });

        Button connectButton = findViewById(R.id.connectButton);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String robotIp = ((EditText) findViewById(R.id.robotIpEt)).getText().toString();
                int robotPort = Integer.parseInt(((EditText) findViewById(R.id.robotPortEt)).getText().toString());

                tcpClient.sendMessage(String.format("CONNECT %s %d", robotIp, robotPort).getBytes());

                Intent myIntent = new Intent(ConnectRobotActivity.this, ManualControlActivity.class);
                ConnectRobotActivity.this.startActivity(myIntent);
            }
        });
    }

    private void onTcpMessage(byte[] message) {

    }

    private void onTcpError(Throwable tr) {
        // TODO
    }

}
