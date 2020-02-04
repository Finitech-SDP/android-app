package uk.ac.ed.inf.finitech;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class ManualControlActivity extends AppCompatActivity {
    private TcpClient tcpClient;
    private int power = 50;
    private int degrees = 90;

    private Button fwdBtn, fwdrBtn, fwdlBtn, bwdBtn, bwdrBtn, bwdlBtn, cwrBtn, acwrBtn, stopBtn;

    private void findButtons() {
        fwdBtn = findViewById(R.id.fwdBtn);
        fwdrBtn = findViewById(R.id.fwdrBtn);
        fwdlBtn = findViewById(R.id.fwdlBtn);
        bwdBtn = findViewById(R.id.bwdBtn);
        bwdrBtn = findViewById(R.id.bwdrBtn);
        bwdlBtn = findViewById(R.id.bwdlBtn);
        cwrBtn = findViewById(R.id.cwrBtn);
        acwrBtn = findViewById(R.id.acwrBtn);
        stopBtn = findViewById(R.id.stopBtn);
    }

    private void setButtonActions() {
        final int defaultTime = 1000;  // msec

        fwdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = String.format(
                        "F %d %d",
                        ManualControlActivity.this.power,
                        defaultTime);
                tcpClient.sendMessage(message.getBytes());
            }
        });
        bwdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = String.format(
                        "B %d %d",
                        ManualControlActivity.this.power,
                        defaultTime);
                tcpClient.sendMessage(message.getBytes());
            }
        });
        fwdrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = String.format(
                        "FR %d %d",
                        ManualControlActivity.this.power,
                        ManualControlActivity.this.degrees);
                tcpClient.sendMessage(message.getBytes());
            }
        });
        fwdlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = String.format(
                        "FL %d %d",
                        ManualControlActivity.this.power,
                        ManualControlActivity.this.degrees);
                tcpClient.sendMessage(message.getBytes());
            }
        });
        bwdrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = String.format(
                        "BR %d %d",
                        ManualControlActivity.this.power,
                        ManualControlActivity.this.degrees);
                tcpClient.sendMessage(message.getBytes());
            }
        });
        bwdlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = String.format(
                        "BL %d %d",
                        ManualControlActivity.this.power,
                        ManualControlActivity.this.degrees);
                tcpClient.sendMessage(message.getBytes());
            }
        });
        cwrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = String.format(
                        "RC %d %d",
                        ManualControlActivity.this.power,
                        ManualControlActivity.this.degrees);
                tcpClient.sendMessage(message.getBytes());
            }
        });
        acwrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = String.format(
                        "RA %d %d",
                        ManualControlActivity.this.power,
                        ManualControlActivity.this.degrees);
                tcpClient.sendMessage(message.getBytes());
            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tcpClient.sendMessage("STOP".getBytes());
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
