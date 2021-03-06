package uk.ac.ed.inf.finitech;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import org.json.JSONException;
import org.json.JSONObject;

public class AutoControl extends AppCompatActivity {
    private String TAG = "Bora - " + AutoControl.class.getSimpleName();
    private TcpClient tcpClient;
    private Button startBtn, stopBtn;
    private EditText carColEt, carRowEt, robotColEt, robotRowEt;
    private Switch deliverSw;

    private void findWidgets() {
        startBtn = findViewById(R.id.auto_startBtn);
        stopBtn = findViewById(R.id.auto_stopBtn);
        carColEt = findViewById(R.id.auto_carColEt);
        carRowEt = findViewById(R.id.auto_carRowEt);
        robotColEt = findViewById(R.id.auto_robotColEt);
        robotRowEt = findViewById(R.id.auto_robotRowEt);
        deliverSw = findViewById(R.id.auto_deliverSw);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_control);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findWidgets();

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AutoControl.this.sendMessage("STOP");
            }
        });

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int carCol, carRow, robotCol, robotRow;

                carCol = Integer.parseInt(carColEt.getText().toString());
                carRow = Integer.parseInt(carRowEt.getText().toString());
                robotCol = Integer.parseInt(robotColEt.getText().toString());
                robotRow = Integer.parseInt(robotRowEt.getText().toString());

                JSONObject message = new JSONObject();
                try {
                    JSONObject data = new JSONObject();
                    JSONObject robotPosition = new JSONObject();
                    JSONObject carPosition = new JSONObject();

                    message.put("TAG", "AUTO");
                    message.put("DATA", data);
                    data.put("mode", deliverSw.isChecked() ? "DELIVER" : "PARK");
                    data.put("robotPosition", robotPosition);
                    robotPosition.put("row", robotRow);
                    robotPosition.put("column", robotCol);
                    data.put("carPosition", carPosition);
                    carPosition.put("row", carRow);
                    carPosition.put("column", carCol);
                } catch (JSONException exc) {
                    Log.e(TAG, "JSON Exception", exc);
                    AutoControl.this.finishAffinity();
                }
                AutoControl.this.sendMessage(message.toString());
            }
        });

        tcpClient = MainActivity.tcpClient;
        if (tcpClient == null) {
            return;
        }

        tcpClient.setEventHandler(new TcpClient.EventHandler() {
            @Override
            public void onConnect() { }

            @Override
            public void onConnectError(Throwable tr) { }

            @Override
            public void onMessage(final byte[] message) {
                AutoControl.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AutoControl.this.onTcpMessage(message);
                    }
                });
            }

            @Override
            public void onOperationalError(final Throwable tr) {
                AutoControl.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AutoControl.this.onTcpError(tr);
                    }
                });
            }
        });
    }

    private void sendMessage(String message) {
        if (tcpClient == null) {
            Log.w(TAG, String.format("mock message  %s", message));
            return;
        }

        tcpClient.sendMessage(message.getBytes());
    }

    private void onTcpMessage(byte[] message) {

    }

    private void onTcpError(Throwable tr) {

    }



}
