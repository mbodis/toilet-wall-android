package com.svb.toiletwall.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.svb.toiletwall.fragment.ConnectionFragment;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

public class ConnectionThreadPool {

    public static final String TAG = ConnectionThreadPool.class.getSimpleName();

    public static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier
    public static final int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    public static final int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    private static final String BT_TOILET_WALL_ADDRESS_1 = "98:D3:21:F4:7D:D3";
    private static final String BT_TOILET_WALL_NAME_1 = "HC-06";

    private static final String BT_TOILET_WALL_ADDRESS_2 = "98:D3:21:F4:7C:A4";
    private static final String BT_TOILET_WALL_NAME_2 = "HC-06";

    private static final String BT_TOILET_WALL_ADDRESS_3 = "98:D3:61:F9:3A:C4";
    private static final String BT_TOILET_WALL_NAME_3 = "HC-06";

    private static final String BT_TOILET_WALL_ADDRESS_4 = "00:21:13:01:F5:79";
    private static final String BT_TOILET_WALL_NAME_4 = "HC-05";

    public static final String[] BT_TW_ADDR = {BT_TOILET_WALL_ADDRESS_1, BT_TOILET_WALL_ADDRESS_2,
            BT_TOILET_WALL_ADDRESS_3, BT_TOILET_WALL_ADDRESS_4};
    public static final String[] BT_TW_NAME = {BT_TOILET_WALL_NAME_1, BT_TOILET_WALL_NAME_2,
            BT_TOILET_WALL_NAME_3, BT_TOILET_WALL_NAME_4};

    private Context ctx;
    private Handler mHandler; // Our main handler that will receive callback notifications
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path
    private BluetoothAdapter mBTAdapter;
    private ArrayList<ConnectedThread> pool;
    private boolean connectedDevices[] = {true, true, true, true};

    public ConnectionThreadPool(Context ctx) {
        this.pool = new ArrayList<>();
        this.ctx = ctx;
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        setupHandler();
    }

    public boolean[] getConnectedDevices() {
        return connectedDevices;
    }

    private void setupHandler() {
        // bt message handler
        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == MESSAGE_READ) {
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                if (msg.what == CONNECTING_STATUS) {
                    if (msg.arg1 == 1)
                        Toast.makeText(ctx, "Connected to Device: " + (String) (msg.obj), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(ctx, "Connection Failed", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private void addConnectionThread(ConnectedThread mConnectedThread) {
        if (mConnectedThread != null) {
            this.pool.add(mConnectedThread);
        }
    }

    public void writeByte(Byte mByte) {
        if (this.pool != null && pool.size() > 0) {
            for (ConnectedThread ct : this.pool) {
                ct.writeByte(mByte);
            }
        }
    }

    public void cancel() {
        if (this.pool != null && pool.size() > 0) {
            for (ConnectedThread ct : this.pool) {
                ct.cancel();
            }
        }
    }

    public void connectToDevice(final String address, final String name, final UUID btModuleUuid, final int idx) {
        Log.i(TAG, "connectToDevice: address:" + address);
        Log.i(TAG, "connectToDevice: name:" + name);

        if (!mBTAdapter.isEnabled()) {
            Toast.makeText(ctx, "Enable Bluetooth please", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(ctx, "Connecting...", Toast.LENGTH_SHORT).show();
        // Get the device MAC address, which is the last 17 chars in the View


        // Spawn a new thread to avoid blocking the GUI one
        new Thread() {
            public void run() {
                boolean success = true;

                BluetoothDevice device = mBTAdapter.getRemoteDevice(address);
                Log.d(TAG, "UUID:" + device.getUuids()[0].getUuid());

                try {
                    mBTSocket = createBluetoothSocket(device, btModuleUuid);
                } catch (IOException e) {
                    success = false;
                    Toast.makeText(ctx, "Socket creation failed", Toast.LENGTH_SHORT).show();
                }
                // Establish the Bluetooth socket connection.
                try {
                    mBTSocket.connect();
                } catch (IOException e) {
                    try {
                        success = false;
                        mBTSocket.close();
                        mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                .sendToTarget();
                    } catch (IOException e2) {
                        //insert code to deal with this
                        Toast.makeText(ctx, "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                }

                if (success) {
                    ConnectedThread mConnectedThread = new ConnectedThread(mBTSocket, mHandler);
                    mConnectedThread.start();
                    addConnectionThread(mConnectedThread);

                    // update btns
                    connectedDevices[idx] = false;
                    ConnectionFragment.refreshButtons(ctx);

                    mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                            .sendToTarget();
                }
            }
        }.start();
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device, UUID btModuleUuid) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, btModuleUuid);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection", e);
        }
        return device.createRfcommSocketToServiceRecord(btModuleUuid);
    }
}
