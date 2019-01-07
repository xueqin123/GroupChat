package qin.xue.mygrouptest;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 管理所有连接
 */
public class ServerManager extends Thread {
    private static final String TAG = "ServerManager";
    private ServerSocket serverSocket;
    private boolean isExit = false;
    private ArrayBlockingQueue<ServerApp> serverAppList;
    private int mPort;
    private OnReceivedListener listener;

    interface OnReceivedListener {
        void onReceive(String name, String msg);
    }


    public ServerManager(int port) {
        super("ServerManager_Thread");
        mPort = port;
        serverAppList = new ArrayBlockingQueue(100);
        start();
    }

    @Override
    public void run() {
        try {
            Log.i(TAG, "开始监听...");
            serverSocket = new ServerSocket(mPort);
            while (!isExit) {
                Socket socket = serverSocket.accept();
                if (socket != null) {
                    ServerApp serverApp = new ServerApp(socket, this);
                    serverAppList.add(serverApp);
                    Log.i(TAG, "accept address: " + socket.getInetAddress()
                            + " port: " + socket.getPort()
                            + "serverAppList size = " + serverAppList.size());
                }
            }
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToAll(ServerApp client, String msg) {
        if (listener != null) {
            listener.onReceive(client.getUserName(), msg);
        }
    }

    public void sendMessageToAll(String msg) {
        for (ServerApp serverApp : serverAppList) {
            serverApp.sendMessage(msg);
        }
    }

    public void setListener(OnReceivedListener listener) {
        this.listener = listener;
    }

    public void onAppClosed(ServerApp serverApp) {
        serverAppList.remove(serverApp);
        Log.i(TAG, "onAppClosed() serverAppList size = " + serverAppList.size());
    }
}
