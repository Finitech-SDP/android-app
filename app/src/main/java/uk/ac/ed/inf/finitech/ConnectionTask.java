package uk.ac.ed.inf.finitech;

import android.os.AsyncTask;
import android.util.Log;

import java.io.UnsupportedEncodingException;

public class ConnectionTask extends AsyncTask<Void, byte[], Void> {
    private TcpClient tcpClient;
    private final String server_ip;
    private final int server_port;

    ConnectionTask(String server_ip, int server_port) {
        this.server_ip = server_ip;
        this.server_port = server_port;
    }

    // SYNCHRONOUS
    protected void sendMessage(byte[] message) {
        tcpClient.sendMessage(message);
    }

    @Override
    protected Void doInBackground(Void... v) {
        tcpClient = new TcpClient(server_ip, server_port, new TcpClient.OnMessageReceived() {
            @Override
            public void messageReceived(byte[] message) {
                publishProgress(message);
            }
        });
        tcpClient.run();

        return null;
    }

    @Override
    protected void onProgressUpdate(byte[]... values) {
        super.onProgressUpdate(values);
        String msg = null;
        try {
            msg = new String(values[0], "ascii");
        } catch (UnsupportedEncodingException exc) {
            Log.w("MainActivity", "received undecodable response", exc);
        }

        Log.d("test", "response " + msg);


    }
}