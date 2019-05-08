package com.example.bluetooththird;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button listen,send,listDevices;
    ListView listView;
    TextView msg_box,status;
    EditText writeMsg;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice[] btArray;

    SendReceive sendReceive;

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;

    static int REQUEST_ENABLE_BLUETOOTH=1;

    public String messageFromBluetooth;
    private static final String APP_NAME = "BTChat";
    private static final UUID MY_UUID = UUID.fromString("8ce255c0-224a-11e0-ac64-0803450c9a66");
    Context context;
    public ArrayList<String> deviceNames= new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if(extras !=null)
        {
            messageFromBluetooth="hallo";
            if(extras.getString("message")!= null){
                String msg=extras.getString("message");
                sendReceive.write(msg.getBytes());
                finish();
                return;
            }
            else if(extras.getString("getMessage")!= null){
                Intent intent = new Intent();
                intent.putExtra("answer", messageFromBluetooth);
                setResult(RESULT_OK, intent);
                finish();
                return;
            }
        }

        setContentView(R.layout.activity_main);
        context= getApplicationContext();

        findViewByIdes();


        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        btArray= new BluetoothDevice[1];
        if(!bluetoothAdapter.isEnabled())
        {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,REQUEST_ENABLE_BLUETOOTH);
        } else {
            requestDiscover();
        }


        implementListeners();

    }

    public void requestDiscover()
    {
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60);
        startActivityForResult(discoverableIntent, 60);
    }

    public void fillDeviceArray(int i){
        int index=0;
        BluetoothDevice[] currentList = btArray;
        btArray= new BluetoothDevice[i];
        for(BluetoothDevice currentDevice : currentList){
            btArray[index]=currentDevice;
            index++;
        }
    }

    private BroadcastReceiver mBroadcastDeviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            int test=0;
            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getName()!=null){

                    for(String name : deviceNames)
                    {
                        if(name.equals(device.getName())){
                            test=1;
                        }
                    }
                    if(test==0){
                        deviceNames.add(device.getName());
                        fillDeviceArray(deviceNames.size());
                        btArray[deviceNames.size()-1]=device;
                    }
                }
                ArrayAdapter<String> mBTDevices = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNames);

                //     DeviceListAdapter deviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                listView.setAdapter(mBTDevices);
            }
            else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                bluetoothAdapter.startDiscovery();
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case 1:
                switch (resultCode) {
                    case 0:
                        CharSequence text = "Bluetooth aktivieren und App neustarten!";
                        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
                        toast.show();
                        finish();
                        break;
                    default:

                        try {
                            requestDiscover();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }
            case 60:
                switch(resultCode){
                    case 0:
                        CharSequence text = "Sie sind nicht sichtbar!";
                        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
                        toast.show();
                        checkPermissions();
                        break;
                    case 60:
                        checkPermissions();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == 0){
                    discoverBluetooth();
                }
                break;
             default:
                 CharSequence text = "Standort aktivieren und App neustarten!";
                 Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
                 toast.show();
                 finish();
                 break;
        }
    }

    private void discoverBluetooth() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();

            bluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastDeviceReceiver, discoverDevicesIntent);
            IntentFilter restartDiscover = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(mBroadcastDeviceReceiver, restartDiscover);
        }
        if(!bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastDeviceReceiver, discoverDevicesIntent);
            IntentFilter restartDiscover = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(mBroadcastDeviceReceiver, restartDiscover);

        }
        ServerClass serverClass= new ServerClass();

        serverClass.start();

    }
    private void checkPermissions(){
        int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

    }
    private void implementListeners() {

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                ClientClass clientClass= new ClientClass(btArray[i]);
                clientClass.start();
                listView.setAdapter(null);
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sendReceive!=null) {
                    String string = String.valueOf(writeMsg.getText());
                    sendReceive.write(string.getBytes());
                }
                else{
                    String text= "nicht verbunden";
                    Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
                    toast.show();
                    try {
                        unregisterReceiver(mBroadcastDeviceReceiver);

                    } catch (IllegalArgumentException e){
                        e.printStackTrace();
                    }
                    bluetoothAdapter.cancelDiscovery();
                }

            }
        });
    }

    Handler deleteList = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            listView.setAdapter(null);
            return true;

        }
    });

    Handler handler= new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            CharSequence text;
            Toast toast;
            switch(msg.what)
            {
                case STATE_LISTENING:
                    break;
                case STATE_CONNECTING:
                    break;
                case STATE_CONNECTED:
                    text = "Verbunden";
                    toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
                    toast.show();
                    break;
                case STATE_CONNECTION_FAILED:
                    text = "Verbindung fehlgeschlagen, versuchen Sie es mit einem Neustart!";
                    toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
                    toast.show();
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuffer= (byte[]) msg.obj;
                    String tempMsg= new String(readBuffer, 0, msg.arg1);
                    msg_box.setText(tempMsg);
                    messageFromBluetooth=tempMsg;
                    break;
            }
            return true;
        }
    });

    private void findViewByIdes() {
        send=(Button) findViewById(R.id.send);
        listView=(ListView) findViewById(R.id.listview);
        msg_box=(TextView) findViewById(R.id.msg);
        writeMsg=(EditText) findViewById(R.id.writemsg);

    }

    private class ServerClass extends Thread
    {
        private BluetoothServerSocket serverSocket;

        public ServerClass() {
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);

            } catch (IOException e) {

            }
        }

        public void run()
           {
               BluetoothSocket socket= null;

               while (socket==null)
               {
                   try {
                       Message message = Message.obtain();
                       message.what=STATE_CONNECTING;
                       handler.sendMessage(message);
                       socket=serverSocket.accept();
                   } catch (IOException e) {
                       e.printStackTrace();

                       Message message = Message.obtain();
                       message.what=STATE_CONNECTION_FAILED;
                       handler.sendMessage(message);
                   }
                   if(socket!=null)
                   {
                       Message message = Message.obtain();
                       message.what=STATE_CONNECTED;
                       handler.sendMessage(message);

                       sendReceive=new SendReceive(socket);
                       sendReceive.start();
                       Message delete = Message.obtain();
                       delete.what= STATE_CONNECTED;
                       deleteList.sendMessage(delete);
                       cancel();
                       try {
                           unregisterReceiver(mBroadcastDeviceReceiver);

                       } catch (IllegalArgumentException e){
                           e.printStackTrace();
                       }
                       bluetoothAdapter.cancelDiscovery();
                       break;
                   }
            }
        }

        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ClientClass extends Thread
    {
        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass (BluetoothDevice device1)
        {
            device=device1;
            try {
                socket= device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                try {
                    unregisterReceiver(mBroadcastDeviceReceiver);

                } catch (IllegalArgumentException e){
                    e.printStackTrace();
                }
                bluetoothAdapter.cancelDiscovery();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run()
        {
            try {
                socket.connect();
                Message message=Message.obtain();
                message.what=STATE_CONNECTED;
                handler.sendMessage(message);

                sendReceive= new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();

                Message message=Message.obtain();
                message.what=STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class SendReceive extends Thread
    {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive(BluetoothSocket socket)
        {
            bluetoothSocket=socket;
            InputStream tempIn= null;
            OutputStream tempOut= null;

            try {
                tempIn=bluetoothSocket.getInputStream();
                tempOut=bluetoothSocket.getOutputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = tempIn;
            outputStream=tempOut;
        }

        public void run()
        {
            byte[] buffer= new byte[1024];
            int bytes;

            while(true)
            {
                try {
                    bytes=inputStream.read(buffer);

                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes)
        {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}