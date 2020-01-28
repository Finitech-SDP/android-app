package uk.ac.ed.inf.finitech;

// Based on https://stackoverflow.com/a/38163121/4466589

import android.util.Log;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

public class TcpClient {
    private static final String TAG = TcpClient.class.getSimpleName();
    private final String serverIp;
    private final int serverPort;
    private OnMessageReceived mMessageListener;
    private DataOutputStream outputStream;

    TcpClient(String ip, int port, OnMessageReceived listener) {
        serverIp = ip;
        serverPort = port;
        mMessageListener = listener;
    }

    // SYNCHRONOUS
    void sendMessage(final byte[] message) {
        if (outputStream == null)
            return;

        try {
            Log.d(TAG, "Sending: " + new String(message, "ascii"));
        } catch (UnsupportedEncodingException exc) {
            Log.w(TAG, "Sending non-ASCII message!");
        }

        try {
            byte[] typeHeader = new byte[1];
            typeHeader[0] = 0x01;
            outputStream.write(typeHeader);
            outputStream.writeInt(message.length);  // is big-endian
            outputStream.write(message);
            outputStream.flush();
        } catch (IOException exc) {
            Log.e(TAG, "sendMessage exception", exc);
        }
    }

    void run() {
        try {
            Socket socket = new Socket(InetAddress.getByName(serverIp), serverPort);
            socket.setTcpNoDelay(true);

            outputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());

            try {
                //in this while the client listens for the messages sent by the server
                for(;;) {
                    byte[] typeField = new byte[1];
                    byte[] lengthField = new byte[4];
                    byte[] message;

                    inputStream.readFully(typeField);
                    inputStream.readFully(lengthField);

                    int length = ByteBuffer.wrap(lengthField).getInt();  // big-endian by default
                    message = new byte[length];
                    inputStream.readFully(message);

                    mMessageListener.messageReceived(message);
                }
            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);
            } finally {
                socket.close();
            }

        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
        }

    }

    public interface OnMessageReceived {
        void messageReceived(byte[] message);
    }
}
