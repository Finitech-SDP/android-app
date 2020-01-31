package uk.ac.ed.inf.finitech;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

import java.util.HashMap;
import java.util.Map;

public class ManualControlActivity extends AppCompatActivity {
    private TcpClient tcpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_control);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tcpClient = MainActivity.tcpClient;
        tcpClient.setEventHandler(new TcpClient.EventHandler() {
            @Override
            public void onMessage(byte[] message) {
                ManualControlActivity.this.onTcpMessage(message);
            }

            @Override
            public void onError(Throwable tr) {
                ManualControlActivity.this.onTcpError(tr);
            }
        });

        // maps from button ids to commands
        final Map<Integer, String> buttonCommands = new HashMap<>();
        buttonCommands.put(R.id.nBtn, "NORTH");
        buttonCommands.put(R.id.nwBtn, "NORTH WEST");
        buttonCommands.put(R.id.wBtn, "WEST");
        buttonCommands.put(R.id.swBtn, "SOUTH WEST");
        buttonCommands.put(R.id.sBtn, "SOUTH");
        buttonCommands.put(R.id.seBtn, "SOUTH EAST");
        buttonCommands.put(R.id.eBtn, "EAST");
        buttonCommands.put(R.id.neBtn, "NORTH EAST");
        buttonCommands.put(R.id.acwBtn, "ANTI CLOCKWISE");
        buttonCommands.put(R.id.cwBtn, "CLOCKWISE");

        for (final Integer id : buttonCommands.keySet()) {
            findViewById(id).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tcpClient.sendMessage(buttonCommands.get(id).getBytes());
                }
            });
        }
    }

    private void onTcpMessage(byte[] message) {
        Snackbar.make(findViewById(android.R.id.content), new String(message), Snackbar.LENGTH_LONG).show();
    }

    private void onTcpError(Throwable tr) {
        Snackbar.make(findViewById(android.R.id.content), tr.toString(), Snackbar.LENGTH_INDEFINITE).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        tcpClient.close();
    }
}
