package qin.xue.client;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ClientActivity extends Activity implements View.OnClickListener,
        ClientApp.OnMessageListener, ClientApp.OnConnectListener {
    private static final String TAG = "ClientActivity";
    private Button connectButton;
    private Button sendButton;
    private EditText messageEditText;
    private EditText hostEditText;
    private EditText portEditText;
    private boolean isConnected;
    private TextView receiveTextView;
    private ClientApp clientApp;
    private String receiveString;
    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_activity_layout);
        connectButton = findViewById(R.id.connect);
        messageEditText = findViewById(R.id.edit);
        hostEditText = findViewById(R.id.host);
        portEditText = findViewById(R.id.port);
        sendButton = findViewById(R.id.send_btn);
        receiveTextView = findViewById(R.id.receive);
        connectButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);
        receiveString = new String();
        userId = Utils.getSystemModel();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_btn:
                send();
                break;
            case R.id.connect:
                if(isConnected){
                    clientApp.exit();
                }else {
                    connect();
                }
                break;
            default:
                break;
        }
    }

    private void connect() {
        String host = hostEditText.getText().toString();
        String port = portEditText.getText().toString();
        Log.i(TAG, "connect() host: " + host + " port: " + port);
        if (clientApp != null) {
            clientApp.exit();
        }
        clientApp = new ClientApp(host, Integer.valueOf(port));
        clientApp.setListener(this);
        clientApp.setConnectListener(this);
        clientApp.setUserId(userId);
    }

    private void send() {
        String msg = messageEditText.getText().toString();
        if (!TextUtils.isEmpty(msg)) {
            sendMsg(msg);
        } else {
            Toast.makeText(this, "输入为空", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMsg(String msg) {
        Log.i(TAG, "sendMsg() msg: " + msg);
        if (clientApp == null) {
            Toast.makeText(this, "先连接服务器", Toast.LENGTH_SHORT).show();
            return;
        }
        clientApp.sendMessage(msg);
    }

    @Override
    public void onReceive(String message) {
        Log.i(TAG, "onReceive() message: " + message);
        receiveString += message;
        receiveString += "\n";
        receiveTextView.setText(receiveString);
    }

    @Override
    public void onSend(String message) {
        Log.i(TAG, "onSend() message: " + message);
    }

    @Override
    public void onConnected() {
        isConnected = true;
        connectButton.setText("断开连接");
    }

    @Override
    public void onDisconnected() {
        isConnected = false;
        connectButton.setText("连接服务器");
    }
}
