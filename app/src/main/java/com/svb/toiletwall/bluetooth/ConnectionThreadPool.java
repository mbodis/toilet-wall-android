package com.svb.toiletwall.bluetooth;

import java.util.ArrayList;

public class ConnectionThreadPool {

    private ArrayList<ConnectedThread> pool;

    public ConnectionThreadPool() {
        this.pool = new ArrayList<>();
    }

    public void addConnectionThread(ConnectedThread mConnectedThread){
        if (mConnectedThread != null){
            this.pool.add(mConnectedThread);
        }
    }

    public void writeByte(Byte mByte){
        if (this.pool != null && pool.size() > 0){
            for (ConnectedThread ct : this.pool) {
                ct.writeByte(mByte);
            }
        }
    }

    public void cancel(){
        if (this.pool != null && pool.size() > 0){
            for (ConnectedThread ct : this.pool) {
                ct.cancel();
            }
        }
    }
}
