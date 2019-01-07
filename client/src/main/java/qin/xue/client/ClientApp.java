package qin.xue.client;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientApp extends Thread {
    private static final String TAG = "ClientApp";

    public interface OnMessageListener {
        void onReceive(String message);

        void onSend(String message);
    }

    public interface OnConnectListener {
        void onConnected();

        void onDisconnected();
    }

    private Handler uiHandler;
    private Handler socktetHandler;

    private String ip;
    private int port;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private PrintWriter printWriter;
    private OnMessageListener listener;
    private OnConnectListener connectListener;
    private String uid;

    public ClientApp(String host, int p) {
        super("client_thread");
        Log.i(TAG, "ClientApp() " + hashCode());
        ip = host;
        port = p;
        uiHandler = new Handler(Looper.getMainLooper());
        HandlerThread handlerThread = new HandlerThread("socket_Send_Thread");
        handlerThread.start();
        socktetHandler = new Handler(handlerThread.getLooper());
        start();
    }

    @Override
    public void run() {
        super.run();
        try {
            socket = new Socket(ip, port);
            socket.setKeepAlive(true);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            printWriter = new PrintWriter(outputStream);
            byte buff[] = new byte[4096];
            int rcvLen;
            if (socket.isConnected()) {
                sendMessage(uid);
                onInnerConnected();
            }
            while (socket.isConnected()
                    && !socket.isInputShutdown()
                    && (rcvLen = inputStream.read(buff)) != -1) {
                String msg = new String(buff, 0, rcvLen, "utf-8");
                Log.i(TAG, "ClientApp receive msg: " + msg);
                onReceivedMessage(msg);
            }
            close();
        } catch (IOException e) {
            Log.i(TAG, "e: " + e);
            e.printStackTrace();
        }
    }

    private void close() {
        Log.i(TAG, "run close " + hashCode());
        try {
            printWriter.close();
            inputStream.close();
            socket.shutdownInput();
            socket.shutdownOutput();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        onInnerDisconnected();
    }

    public void exit() {
        close();
    }

    public void sendMessage(final String msg) {
        socktetHandler.post(new Runnable() {
            @Override
            public void run() {
                printWriter.println(msg);
                printWriter.flush();
                onSendMessage(msg);
            }
        });
    }

    public void setListener(OnMessageListener listener) {
        this.listener = listener;
    }

    @Override
    protected void finalize() throws Throwable {
        Log.i(TAG, "finalize() " + hashCode());
        super.finalize();
    }

    public void setConnectListener(OnConnectListener connectListener) {
        this.connectListener = connectListener;
    }

    private void onInnerConnected() {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (connectListener != null) {
                    connectListener.onConnected();
                }
            }
        });
    }

    private void onInnerDisconnected() {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (connectListener != null) {
                    connectListener.onDisconnected();
                }
            }
        });
    }


    public void onReceivedMessage(final String msg) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "run onReceivedMessage");
                if (!TextUtils.isEmpty(msg) && listener != null) {
                    listener.onReceive(msg);
                }
            }
        });
    }

    public void onSendMessage(final String msg) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(msg) && listener != null) {
                    listener.onSend(msg);
                }
            }
        });
    }

    public void setUserId(String uid) {
        this.uid = uid;
    }
}
