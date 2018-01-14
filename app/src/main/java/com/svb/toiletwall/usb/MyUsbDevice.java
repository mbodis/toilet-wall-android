package com.svb.toiletwall.usb;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.widget.Toast;

import com.svb.toiletwall.support.FileLog;

import java.util.HashMap;
import java.util.Iterator;


/**
 * Created by mbodis on 2/12/16.
 */
public class MyUsbDevice {

    public static final String TAG = MyUsbDevice.class.getName();

    private static final int MY_USB_VENDOR_ID = 10862;
    private static final int MY_USB_PRODUCT_ID = 32771;


    public static void findDeviceAndStartService(Context ctx) {
        UsbManager usbManager = (UsbManager) ctx.getSystemService(Context.USB_SERVICE);
        UsbDevice usbDevice = null;
        HashMap<String, android.hardware.usb.UsbDevice> usbDeviceList = usbManager.getDeviceList();
        FileLog.addLog(ctx, TAG, "length: " + usbDeviceList.size());
        Iterator<android.hardware.usb.UsbDevice> deviceIterator = usbDeviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            android.hardware.usb.UsbDevice tempUsbDevice = deviceIterator.next();

            // Print device information. If you think your device should be able
            // to communicate with this app, add it to accepted products below.
            FileLog.addLog(ctx, TAG, "VendorId: " + tempUsbDevice.getVendorId());
            FileLog.addLog(ctx, TAG, "ProductId: " + tempUsbDevice.getProductId());
            FileLog.addLog(ctx, TAG, "DeviceName: " + tempUsbDevice.getDeviceName());
            FileLog.addLog(ctx, TAG, "DeviceId: " + tempUsbDevice.getDeviceId());
            FileLog.addLog(ctx, TAG, "DeviceClass: " + tempUsbDevice.getDeviceClass());
            FileLog.addLog(ctx, TAG, "DeviceSubclass: " + tempUsbDevice.getDeviceSubclass());
            FileLog.addLog(ctx, TAG, "InterfaceCount: " + tempUsbDevice.getInterfaceCount());
            FileLog.addLog(ctx, TAG, "DeviceProtocol: " + tempUsbDevice.getDeviceProtocol());

            if (tempUsbDevice.getVendorId() == MY_USB_VENDOR_ID){
                FileLog.addLog(ctx, TAG, "My device found!");

                if (tempUsbDevice.getProductId() == MY_USB_PRODUCT_ID) {
                    Toast.makeText(ctx, "My usb device found", Toast.LENGTH_SHORT).show();
                    usbDevice = tempUsbDevice;
                }
            }
        }

        if (usbDevice == null) {
            FileLog.addLog(ctx, TAG, "No device found!");
            Toast.makeText(ctx, "no_device_found", Toast.LENGTH_LONG).show();
        } else {
            FileLog.addLog(ctx, TAG, "Device found!");
            Intent startIntent = new Intent(ctx, UsbDeviceService.class);
            PendingIntent pendingIntent = PendingIntent.getService(ctx, 0, startIntent, 0);
            usbManager.requestPermission(usbDevice, pendingIntent);
        }
    }

}
