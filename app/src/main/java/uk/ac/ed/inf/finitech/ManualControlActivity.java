package uk.ac.ed.inf.finitech;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class ManualControlActivity extends AppCompatActivity {
    private TcpClient tcpClient;
    private int power = 50;
    private int degrees = 90;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_control);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // maps from button ids to commands
        final Map<Integer, String> buttonCommands = new HashMap<>();
        buttonCommands.put(R.id.nBtn, "N");
        buttonCommands.put(R.id.nwBtn, "NW");
        buttonCommands.put(R.id.swBtn, "SW");
        buttonCommands.put(R.id.sBtn, "S");
        buttonCommands.put(R.id.seBtn, "SE");
        buttonCommands.put(R.id.neBtn, "NE");
        buttonCommands.put(R.id.acwBtn, "AR");
        buttonCommands.put(R.id.cwBtn, "CR");
        buttonCommands.put(R.id.stopBtn, "STOP");

        for (final Integer id : buttonCommands.keySet()) {
            findViewById(id).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String message = MessageFormat.format(
                            "{0} {1} {2}",
                            buttonCommands.get(id),
                            ManualControlActivity.this.power,
                            ManualControlActivity.this.degrees
                    );
                    tcpClient.sendMessage(message.getBytes());
                }
            });
        }

        ((SeekBar) findViewById(R.id.powerSB)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ((TextView) findViewById(R.id.powerValTV)).setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ManualControlActivity.this.power = seekBar.getProgress();
            }
        });

        ((SeekBar) findViewById(R.id.degreesSB)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ((TextView) findViewById(R.id.degreesValTV)).setText("" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ManualControlActivity.this.degrees = seekBar.getProgress();
            }
        });

        tcpClient = MainActivity.tcpClient;
        tcpClient.setEventHandler(new TcpClient.EventHandler() {
            @Override
            public void onConnect() { }

            @Override
            public void onConnectError(Throwable tr) { }

            @Override
            public void onMessage(byte[] message) {
                ManualControlActivity.this.onTcpMessage(message);
            }

            @Override
            public void onOperationalError(Throwable tr) {
                ManualControlActivity.this.onTcpError(tr);
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
