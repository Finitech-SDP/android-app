package uk.ac.ed.inf.finitech;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class ManualControlActivity extends AppCompatActivity {
    private static String TAG = "ManualControlActivity";
    private TcpClient tcpClient;

    private Button stopBtn;

    private void findButtons() {
        stopBtn = findViewById(R.id.stopBtn);
    }

    private void setButtonActions() {
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tcpClient != null)
                    tcpClient.sendMessage("STOP".getBytes());
                else
                    Log.i(TAG, "STOP");
            }
        });
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_control);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findButtons();
        setButtonActions();

        JoystickView joystick = findViewById(R.id.joystick);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                // do whatever you want
                if (tcpClient != null)
                    tcpClient.sendMessage(String.format("MOVE %d %d", angle, strength).getBytes());
                else
                    Log.i(TAG, String.format("MOVE %d %d", angle, strength));
            }
        }, 200 /* ms */);

        JoystickView joystick2 = findViewById(R.id.joystick2);
        joystick2.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                // do whatever you want
                if (tcpClient != null)
                    tcpClient.sendMessage(String.format("ROTATE %d %d", angle, strength).getBytes());
                else
                    Log.i(TAG, String.format("ROTATE %d %d", angle, strength));
            }
        }, 200 /* ms */);

        tcpClient = MainActivity.tcpClient;
        if (tcpClient == null)
            return;
        tcpClient.setEventHandler(new TcpClient.EventHandler() {
            @Override
            public void onConnect() { }

            @Override
            public void onConnectError(Throwable tr) { }

            @Override
            public void onMessage(final byte[] message) {
                ManualControlActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ManualControlActivity.this.onTcpMessage(message);
                    }
                });
            }

            @Override
            public void onOperationalError(final Throwable tr) {
                ManualControlActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ManualControlActivity.this.onTcpError(tr);
                    }
                });
            }
        });
    }

    private void onTcpMessage(byte[] message) {
        Snackbar.make(findViewById(android.R.id.content), new String(message), Snackbar.LENGTH_LONG).show();
    }

    private void onTcpError(Throwable tr) {
        Snackbar.make(findViewById(android.R.id.content), "Operational Error", Snackbar.LENGTH_INDEFINITE).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tcpClient.close();
    }
}
