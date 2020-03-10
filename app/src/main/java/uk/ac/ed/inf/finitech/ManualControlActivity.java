package uk.ac.ed.inf.finitech;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ManualControlActivity extends AppCompatActivity {
    private static String TAG = "ManualControlActivity";
    private TcpClient tcpClient;

    private Switch liftSwitch;
    private ToggleButton nBtn, neBtn, eBtn, seBtn, sBtn, swBtn, wBtn, nwBtn, cwBtn, acwBtn;
    private Button stopBtn, autoBtn;

    private void findWidgets() {
        liftSwitch = findViewById(R.id.liftSwitch);
        nBtn = findViewById(R.id.nBtn);
        neBtn = findViewById(R.id.neBtn);
        eBtn = findViewById(R.id.eBtn);
        seBtn = findViewById(R.id.seBtn);
        sBtn = findViewById(R.id.sBtn);
        swBtn = findViewById(R.id.swBtn);
        wBtn = findViewById(R.id.wBtn);
        nwBtn = findViewById(R.id.nwBtn);
        cwBtn = findViewById(R.id.cwBtn);
        acwBtn = findViewById(R.id.acwBtn);
        stopBtn = findViewById(R.id.stopBtn);
        autoBtn = findViewById(R.id.autoBtn);
    }

    private void setEventHandlers() {
        final Map<ToggleButton, String> commandMap = new HashMap<>();
        commandMap.put(nBtn, "F");
        commandMap.put(neBtn, "FR");
        commandMap.put(eBtn, "R");
        commandMap.put(seBtn, "BR");
        commandMap.put(sBtn, "B");
        commandMap.put(swBtn, "BL");
        commandMap.put(wBtn, "L");
        commandMap.put(nwBtn, "FL");
        commandMap.put(cwBtn, "RC");
        commandMap.put(acwBtn, "RA");

        for (final ToggleButton button: commandMap.keySet()) {
            button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (!isChecked) {
                        sendMessage("STOP");
                        liftSwitch.setEnabled(true);
                        return;
                    }

                    // Uncheck all the other ToggleButtons
                    for (ToggleButton b : commandMap.keySet()) {
                        if (!button.equals(b)) {
                            b.setChecked(false);
                        }
                    }

                    // Disable lift Switch
                    liftSwitch.setEnabled(false);

                    ManualControlActivity.this.sendMessage(
                            String.format("%s  -F", commandMap.get(button))
                    );
                }
            });
        }

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Uncheck all ToggleButtons
                for (ToggleButton b : commandMap.keySet()) {
                    b.setChecked(false);
                }

                ManualControlActivity.this.sendMessage("STOP");
            }
        });

        autoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(ManualControlActivity.this, AutoControl.class);
                ManualControlActivity.this.startActivity(myIntent);
            }
        });
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_control);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findWidgets();
        setEventHandlers();

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

    private void sendMessage(String message) {
        if (tcpClient == null) {
            Log.w(TAG, String.format("mock message  %s", message));
            return;
        }

        JSONObject msg = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            msg.put("TAG", "RELAY-ASCII");
            msg.put("DATA", data);
            data.put("message", message);
        } catch (JSONException exc) {
            Log.e(TAG, "JSON error", exc);
            this.finishAffinity();
        }

        tcpClient.sendMessage(msg.toString().getBytes());
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
