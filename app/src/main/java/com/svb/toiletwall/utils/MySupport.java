package com.svb.toiletwall.utils;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by mbodis on 8/18/17.
 */

public class MySupport {

    public static final int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names

    public static boolean setBluetoothOn(Activity act) {
        BluetoothAdapter mBTAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBTAdapter == null) {
            Toast.makeText(act, "Bluetooth device not found!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            act.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Toast.makeText(act, "Bluetooth turned on", Toast.LENGTH_SHORT).show();
            return true;

        } else {
            Toast.makeText(act, "Bluetooth is already on", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    public static boolean setBluetoothOff(Activity act) {
        BluetoothAdapter mBTAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBTAdapter == null) {
            Toast.makeText(act, "Bluetooth device not found!", Toast.LENGTH_SHORT).show();
            return false;
        }

        mBTAdapter.disable(); // turn off
        Toast.makeText(act, "Bluetooth turned Off", Toast.LENGTH_SHORT).show();

        return true;
    }

}



