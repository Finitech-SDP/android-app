package uk.ac.ed.inf.finitech;

// Based on https://stackoverflow.com/a/38163121/4466589

import android.util.Log;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class TcpClient implements Runnable {
    private static final String TAG = "Bora - " + TcpClient.class.getSimpleName();
    private TcpClientState state;
    private final String serverIp;
    private final int serverPort;
    private EventHandler eventHandler;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;
    private Socket socket;

    private final ReentrantLock reentrantLock = new ReentrantLock();
    private final Condition condition = reentrantLock.newCondition();

    private final BlockingQueue<byte[]> outQueue = new LinkedBlockingQueue<>(1);

    TcpClient(String ip, int port) {
        state = TcpClientState.START;
        serverIp = ip;
        serverPort = port;
    }

    void setEventHandler(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    /**
     * Not synchronous, but will block if there is already a message in the queue!
     * @param message
     */
    void sendMessage(final byte[] message) {
        if (state != TcpClientState.OPERATING) {
            Log.e(TAG, String.format("sendMessage() is called but state is not OPERATING (is %s)", state));
            return;
        }

        try {
            Log.d(TAG, "Sending: " + new String(message, "ascii"));
        } catch (UnsupportedEncodingException exc) {
            Log.w(TAG, "Sending non-ASCII message!");
        }

        try {
            outQueue.put(message);
        } catch (InterruptedException exc) {
            Log.e(TAG, "outQueue.put is interrupted!", exc);
        }
    }

    public void run() {
        if (state != TcpClientState.START) {
            Log.e(TAG, String.format("run() is called but state is not START (is %s)", state));
            return;
        }

        state = TcpClientState.CONNECTING;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(serverIp, serverPort), 3000);
            socket.setTcpNoDelay(true);

            outputStream = new DataOutputStream(socket.getOutputStream());
            inputStream = new DataInputStream(socket.getInputStream());
        } catch (Throwable tr) {
            state = TcpClientState.CONNECT_ERROR;
            Log.e(TAG, "connect()", tr);
            eventHandler.onConnectError(tr);
            return;
        }

        state = TcpClientState.CONNECTED;
        eventHandler.onConnect();

        try {
            reentrantLock.lock();
            condition.await();
        } catch (InterruptedException e) {
            Log.e(TAG, "thread interrupted", e);
            close();
            state = TcpClientState.CLOSED;
            return;
        } finally {
            reentrantLock.unlock();
        }

        state = TcpClientState.OPERATING;

        Runnable sender = new Runnable() {
            @Override
            public void run() {
                for (;;) {
                    try {
                        byte[] message = outQueue.take();
                        try {
                            outputStream.writeByte((byte) 0x01b);
                            outputStream.writeInt(message.length);  // is big-endian
                            outputStream.write(message);
                            outputStream.flush();
                        } catch (IOException exc) {
                            state = TcpClientState.OPERATIONAL_ERROR;
                            Log.e(TAG, "sendMessage exception", exc);
                            eventHandler.onOperationalError(exc);
                        }
                    } catch (InterruptedException ignored) {
                        break;
                    }
                }
            }
        };
        Thread senderThread = new Thread(sender);
        senderThread.start();

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
            state = TcpClientState.OPERATIONAL_ERROR;
            Log.e(TAG, "run()", tr);
            eventHandler.onOperationalError(tr);
        }
    }

    void startOperating() {
        try {
            reentrantLock.lock();
            condition.signal();
        } finally {
            reentrantLock.unlock();
        }
    }

    void close() {
        try {
            socket.close();
        } catch (Throwable tr) {
            Log.e(TAG, "close()", tr);
            // Ignore close() errors
        }

        socket = null;
        outputStream = null;
        inputStream = null;
    }

    TcpClientState getState() {
        return state;
    }

    public interface EventHandler {
        void onConnect();
        void onConnectError(Throwable tr);
        void onMessage(byte[] message);
        void onOperationalError(Throwable tr);
    }

    public enum TcpClientState {
        START,
        CONNECTING,
        CONNECTED,
        OPERATING,
        CLOSED,
        CONNECT_ERROR,
        OPERATIONAL_ERROR
    }
}
