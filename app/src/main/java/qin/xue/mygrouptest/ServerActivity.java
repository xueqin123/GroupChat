package qin.xue.mygrouptest;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ServerActivity extends Activity implements ServerManager.OnReceivedListener {
    private TextView mHost;
    private TextView mClients;
    private int mPort = 1234;
    private ServerManager mServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_activity);
        mHost = findViewById(R.id.ip_tv);
        mClients = findViewById(R.id.show_clients);
        mServer = new ServerManager(mPort);
        mServer.setListener(this);
        mHost.setText(Utils.getHostIP() + ":" + mPort);
    }


    @Override
    public void onReceive(String name, String msg) {
        mServer.sendMessageToAll(name + ": " + msg);
    }
}
