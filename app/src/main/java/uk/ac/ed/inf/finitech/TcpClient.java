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

class TcpClient implements Runnable {
    private static final String TAG = TcpClient.class.getSimpleName();
    private final String serverIp;
    private final int serverPort;
    private final EventHandler eventHandler;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;
    private Socket socket;

    TcpClient(String ip, int port, EventHandler listener) {
        serverIp = ip;
        serverPort = port;
        eventHandler = listener;
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
            outputStream.writeByte((byte) 0x01b);
            outputStream.writeInt(message.length);  // is big-endian
            outputStream.write(message);
            outputStream.flush();
        } catch (IOException exc) {
            Log.e(TAG, "sendMessage exception", exc);
            eventHandler.onError(exc);
        }
    }

    public void run() {
        try {
            socket = new Socket(InetAddress.getByName(serverIp), serverPort);
            socket.setTcpNoDelay(true);

            outputStream = new DataOutputStream(socket.getOutputStream());
            inputStream = new DataInputStream(socket.getInputStream());
        } catch (Throwable tr) {
            Log.e(TAG, "connect()", tr);
            eventHandler.onError(tr);
        }

        try {
            for (;;) {
                byte[] typeField = new byte[1];
                byte[] lengthField = new byte[4];
                byte[] message;

                inputStream.readFully(typeField);
                inputStream.readFully(lengthField);

                int length = ByteBuffer.wrap(lengthField).getInt();  // big-endian by default
                message = new byte[length];
                inputStream.readFully(message);

                eventHandler.onMessage(message);
            }
        } catch (Throwable tr) {
            Log.e(TAG, "run()", tr);
            eventHandler.onError(tr);
        }
    }

    void close() {
        try {
            socket.close();
        } catch (Throwable tr) {
            Log.e(TAG, "close()", tr);
            eventHandler.onError(tr);
        }

        socket = null;
        outputStream = null;
        inputStream = null;
    }

    public interface EventHandler {
        void onMessage(byte[] message);
        void onError(Throwable tr);
    }
}
