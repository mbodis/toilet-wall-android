package com.svb.toiletwall.usb;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.svb.toiletwall.support.FileLog;


/**
 * Created by mbodis on 2/12/16.
 */
/*
 * Copyright (C) 2012 Mathias Jeppsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class UsbDeviceService extends Service {

    private final static String TAG = UsbDeviceService.class.getName();
    private final static boolean DEBUG = true;

    private boolean mIsRunning = false;

    private volatile UsbDevice mUsbDevice = null;
    private volatile UsbDeviceConnection mUsbConnection = null;
    private volatile UsbEndpoint mInUsbEndpoint = null;
    private volatile UsbEndpoint mOutUsbEndpoint = null;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        if (DEBUG) FileLog.addLog(getApplicationContext(), TAG, "onCreate()");
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) FileLog.addLog(getApplicationContext(), TAG, "onStartCommand() " + intent + " " + flags + " " + startId);

        if (mIsRunning) {
            if (DEBUG) FileLog.addLog(getApplicationContext(), TAG, "Service already running.");
            return Service.START_REDELIVER_INTENT;
        }

        mIsRunning = true;

        if (!intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
            if (DEBUG) FileLog.addLog(getApplicationContext(), TAG, "Permission denied");
            Toast.makeText(getBaseContext(), "permission_denied", Toast.LENGTH_LONG).show();
            stopSelf();
            return Service.START_REDELIVER_INTENT;
        }

        if (DEBUG) FileLog.addLog(getApplicationContext(), TAG, "Permission granted");
        mUsbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

        if (mUsbDevice == null){
            FileLog.addLog(getApplicationContext(), TAG, "mUsbDevice == null");
        }else{
            FileLog.addLog(getApplicationContext(), TAG, "mUsbDevice != null");
            FileLog.addLog(getApplicationContext(), TAG, "mUsbDevice.getDeviceName: " + mUsbDevice.getDeviceName());
            FileLog.addLog(getApplicationContext(), TAG, "mUsbDevice.getSerialNumber: " + mUsbDevice.getSerialNumber());
            FileLog.addLog(getApplicationContext(), TAG, "mUsbDevice.getDeviceProtocol: " + mUsbDevice.getDeviceProtocol());
        }
        if (!initDevice()) {
            if (DEBUG) FileLog.addLog(getApplicationContext(), TAG, "Init of device failed!");
            stopSelf();
            return Service.START_REDELIVER_INTENT;
        }

        if (DEBUG) FileLog.addLog(getApplicationContext(), TAG, "Receiving!");
        Toast.makeText(getBaseContext(), "receiving", Toast.LENGTH_SHORT).show();
        startReceiverThread();
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        if (DEBUG) FileLog.addLog(getApplicationContext(), TAG, "onDestroy()");
        super.onDestroy();
        unregisterReceiver(mReceiver);
        mUsbDevice = null;
        if (mUsbConnection != null) {
            mUsbConnection.close();
        }
    }

    private boolean initDevice() {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mUsbConnection = usbManager.openDevice(mUsbDevice);

        if (mUsbConnection == null) {
            if (DEBUG) FileLog.addLog(getApplicationContext(), TAG, "Opening USB device failed!");
            Toast.makeText(getBaseContext(), "opening_device_failed", Toast.LENGTH_LONG).show();
            return false;
        }else{
            if (DEBUG) FileLog.addLog(getApplicationContext(), TAG, "Opening USB success!");
        }

        FileLog.addLog(getApplicationContext(), TAG, "getInterfaceCount: " + mUsbDevice.getInterfaceCount());
        for(int iface=0; iface < mUsbDevice.getInterfaceCount(); iface++) {
            UsbInterface usbInterface = mUsbDevice.getInterface(iface);

            FileLog.addLog(getApplicationContext(), TAG, "usbInterface.getInterfaceClass(): " + usbInterface.getInterfaceClass());
            FileLog.addLog(getApplicationContext(), TAG, "usbInterface.getInterfaceSubclass(): " + usbInterface.getInterfaceSubclass());
            FileLog.addLog(getApplicationContext(), TAG, "usbInterface.getInterfaceSubclass(): " + usbInterface.getInterfaceProtocol());

            // find right interface
            FileLog.addLog(getApplicationContext(), TAG, "usbInterface.getEndpointCount(): " + usbInterface.getEndpointCount());
            for (int i = 0; i < usbInterface.getEndpointCount(); i++) {

                FileLog.addLog(getApplicationContext(), TAG, "usbInterface.getEndpoint(i).getType(): " + usbInterface.getEndpoint(i).getType());
                FileLog.addLog(getApplicationContext(), TAG, "usbInterface.getEndpoint(i).getDirection(): " + usbInterface.getEndpoint(i).getDirection());

                if (usbInterface.getEndpoint(i).getType() == UsbConstants.USB_CLASS_HID) {
                    FileLog.addLog(getApplicationContext(), TAG, "USB class for human interface devices (for example, mice and keyboards)");
                }

                if (usbInterface.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK || usbInterface.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_INT) {
                    if (usbInterface.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN) {
                        mInUsbEndpoint = usbInterface.getEndpoint(i);
                    } else if (usbInterface.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_OUT) {
                        mOutUsbEndpoint = usbInterface.getEndpoint(i);
                    }
                }
            }

            if (mInUsbEndpoint == null || mOutUsbEndpoint == null){
                if (mInUsbEndpoint == null)
                    if (DEBUG) FileLog.addLog(getApplicationContext(), TAG, "No in endpoint found!");
                if (mOutUsbEndpoint == null)
                    if (DEBUG) FileLog.addLog(getApplicationContext(), TAG, "No out endpoint found!");

                mInUsbEndpoint = null;
                mOutUsbEndpoint = null;
            }else{

                if (!mUsbConnection.claimInterface(usbInterface, true)) {
                    if (DEBUG) FileLog.addLog(getApplicationContext(), TAG, "Claiming interface failed!");
                    Toast.makeText(getBaseContext(), "claimning_interface_failed", Toast.LENGTH_LONG).show();
                    mUsbConnection.close();
                    return false;
                }

                break;
            }
        }

        if (mInUsbEndpoint == null) {
            if (DEBUG) FileLog.addLog(getApplicationContext(), TAG, "No in endpoint found!");
            Toast.makeText(getBaseContext(), "no_in_endpoint_found", Toast.LENGTH_LONG).show();
            mUsbConnection.close();
            return false;
        }

        if (mOutUsbEndpoint == null) {
            if (DEBUG) FileLog.addLog(getApplicationContext(), TAG, "No out endpoint found!");
            Toast.makeText(getBaseContext(), "no_out_endpoint_found", Toast.LENGTH_LONG).show();
            mUsbConnection.close();
            return false;
        }

        mUsbConnection.controlTransfer(0x21, 34, 0, 0, null, 0, 0);
        mUsbConnection.controlTransfer(0x21, 32, 0, 0, new byte[] { (byte) 0x80,
                0x25, 0x00, 0x00, 0x00, 0x00, 0x08 }, 7, 0);

        return true;
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (DEBUG) FileLog.addLog(getApplicationContext(), TAG, "onReceive() " + action);

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                Toast.makeText(context, "device_detaches", Toast.LENGTH_LONG).show();
                stopSelf();
            }
        }
    };

    private void startReceiverThread() {
        if (DEBUG) FileLog.addLog(getApplicationContext(), TAG, "startReceiverThread");
        new Thread("arduino_receiver") {
            public void run() {
                final int maxPacketSize = mInUsbEndpoint.getMaxPacketSize();
                byte[] inBuffer = new byte[4096];
                while(mUsbDevice != null ) {
                    //int length = mUsbConnection.bulkTransfer(usbEndpoint, bulkReadBuffer, maxPacketSize, 10);
                    int len = mUsbConnection.bulkTransfer(mInUsbEndpoint, inBuffer, inBuffer.length, 10);
                    if (len > 0) {
                        if (DEBUG) FileLog.addLog(getApplicationContext(), TAG, "data read length: " + len);
                        byte[] buffer = new byte[len];
                        System.arraycopy(inBuffer, 0, buffer, 0, len);

                        FileLog.addLog(getApplicationContext(), TAG, "new data: " + buffer.length
                                + (buffer.length > 0 ? buffer[0] : "X0")
                                + (buffer.length >= 1 ? buffer[1] : "X1")
                                + (buffer.length >= 2 ? buffer[2] : "X2")
                        );

                    } else {
                        if (DEBUG) FileLog.addLog(getApplicationContext(), TAG, "zero data read!");
                    }
                }

                if (DEBUG) FileLog.addLog(getApplicationContext(), TAG, "receiver thread stopped.");
            }
        }.start();
    }

}
