package qin.xue.mygrouptest;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

/**
 * 每一个 ServerApp 对应一个 ClientSocket
 */
public class ServerApp extends Thread {
    private static final String TAG = "ServerApp";
    private Socket mSocket;
    private String name = "default";
    private boolean isExit = false;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private ServerManager serverManager;
    private PrintWriter printWriter;
    private boolean isFrist = true;

    public ServerApp(Socket socket, ServerManager manager) {
        try {
            serverManager = manager;
            mSocket = socket;
            Log.i(TAG, "新的 ServerApp " + hashCode());
            mInputStream = mSocket.getInputStream();
            mOutputStream = mSocket.getOutputStream();
            printWriter = new PrintWriter(mOutputStream);
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        byte buff[] = new byte[4096];
        int rcvLen;
        try {
            while (!isExit && !mSocket.isClosed()
                    && !mSocket.isInputShutdown()
                    && ((rcvLen = mInputStream.read(buff)) != -1)) {
                String msg  = new String(buff, 0, rcvLen, "utf-8");
                Log.i(TAG, "recevie msg " + msg);
                if (isFrist) {
                    name = msg;
                    isFrist = false;
                } else {
                    serverManager.sendMessageToAll(this, msg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mSocket.close();
            mOutputStream.close();
            mInputStream.close();
            serverManager.onAppClosed(this);
            Log.i(TAG, "run: 断开连接");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void close() {

    }

    public void sendMessage(String msg) {
        Log.i(TAG, "sendMessage() msg: " + msg);
        String str = msg;
        printWriter.write(str);
        printWriter.flush();
    }

    public String getUserName() {
        return name;
    }

    @Override
    protected void finalize() throws Throwable {
        Log.i(TAG, "finalize() " + hashCode());
        super.finalize();
    }
}
