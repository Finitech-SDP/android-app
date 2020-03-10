package uk.ac.ed.inf.finitech;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import uk.ac.ed.inf.finitech.structs.Robot;

public class ConnectRobotActivity extends AppCompatActivity {
    private final String TAG = "Bora - " + ConnectRobotActivity.class.getSimpleName();
    TcpClient tcpClient;
    private ArrayAdapter<Robot> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_robot);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tcpClient = MainActivity.tcpClient;
        if (tcpClient == null) {
            Intent myIntent = new Intent(this, ManualControlActivity.class);
            this.startActivity(myIntent);
            return;
        }

        tcpClient.setEventHandler(new TcpClient.EventHandler() {
            @Override
            public void onConnect() {
            }

            @Override
            public void onConnectError(Throwable tr) {
            }

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

        final List<Robot> arrayList = new ArrayList<>();
        adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, arrayList);
        ListView listView = findViewById(R.id.cr_listview);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Robot robot = adapter.getItem((int) id);
                if (robot == null) {
                    throw new Error("Chosen unknown robot!");
                }

                ConnectRobotActivity.this.selectRobot(robot.id);
            }
        });

        JSONObject message = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            message.put("TAG", "LIST-ROBOTS");
            message.put("DATA", data);
        } catch (JSONException exc) {
            Log.e(TAG, "JSON", exc);
            this.finishAffinity();
        }

        tcpClient.sendMessage(message.toString().getBytes());
    }

    private void selectRobot(String id) {
        JSONObject message = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            message.put("TAG", "SELECT-ROBOT");
            message.put("DATA", data);
            data.put("id", id);
        } catch (JSONException exc) {
            Log.e(TAG, "json build error", exc);
            ConnectRobotActivity.this.finishAffinity();
        }
        tcpClient.sendMessage(message.toString().getBytes());
    }

    private void onTcpMessage(byte[] data) {
        try {
            JSONObject message = new JSONObject(new String(data));
            String tag = message.getString("TAG");

            switch (tag) {
                case "LIST-ROBOTS-RES":
                    JSONArray robots = (JSONArray) ((JSONObject) message.get("DATA")).get("robots");

                    // If there is only one robot to "select", select it automatically. =)
                    if (robots.length() == 1) {
                        this.selectRobot(((JSONObject) robots.get(0)).getString("id"));
                    }

                    for (int i = 0; i < robots.length(); i++) {
                        JSONObject robot = (JSONObject) robots.get(i);
                        adapter.add(new Robot(
                                robot.getString("id"),
                                robot.getString("name"),
                                robot.getBoolean("isControlled"))
                        );
                    }
                    break;

                case "SELECT-ROBOT-ACK":
                    Intent myIntent = new Intent(ConnectRobotActivity.this, ManualControlActivity.class);
                    ConnectRobotActivity.this.startActivity(myIntent);
                    break;

                default:
                    Log.w(TAG, String.format("Unexpected response: %s", message.get("TAG")));
                    Snackbar.make(findViewById(android.R.id.content), "Unexpected response from the server.", Snackbar.LENGTH_INDEFINITE).show();
            }
        } catch (JSONException exc) {
            Log.e(TAG, "JSON", exc);
            this.finishAffinity();
        }
    }

    private void onTcpError(Throwable tr) {
        Snackbar.make(findViewById(android.R.id.content), "Operational Error", Snackbar.LENGTH_INDEFINITE).show();
    }

}
