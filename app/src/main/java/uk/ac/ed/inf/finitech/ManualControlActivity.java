package uk.ac.ed.inf.finitech;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;

import java.util.HashMap;
import java.util.Map;

public class ManualControlActivity extends AppCompatActivity {
    private ConnectionTask connectionTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_control);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String serverIp = intent.getStringExtra("serverIp");
        int serverPort = intent.getIntExtra("serverPort", 0);

        connectionTask = new ConnectionTask(serverIp, serverPort);
        connectionTask.execute();

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
                    connectionTask.sendMessage(buttonCommands.get(id).getBytes());
                }
            });
        }
    }

}
