package com.svb.toiletwall.model;

import com.svb.toiletwall.bluetooth.ConnectedThread;

/**
 * Created by mbodis on 8/18/17.
 */

public class ToiletDisplay {
    public static final int WIDTH = 4;
    public static final int HEIGHT = 3;

    byte DELIMETER_1 = (byte)0;
    byte DELIMETER_2 = (byte)255;
    byte DELIMETER_3 = (byte)255;
    byte ROWS_TOTAL = (byte)2;
    byte COLUMNS_TOTAL = (byte)2;


    private boolean[][] display;
    private int rows, columns;

    public ToiletDisplay(int columns, int rows){
        display = new boolean[WIDTH*columns][HEIGHT*rows];
        this.rows = rows;
        this.columns = columns;
        clearScreen();
    }

    public void clearScreen(){
        for (int c=0; c<this.columns; c++){
            for (int r=0; r<this.rows; r++){
                display[c][r] = false;
            }
        }
    }

    public void sendScreenViaBt(ConnectedThread mConnectedThread){
        if (mConnectedThread == null) return;
        test(mConnectedThread);
        // TODO continue
    }

    public void test(ConnectedThread mConnectedThread){
        mConnectedThread.writeByte(DELIMETER_1);
        mConnectedThread.writeByte(DELIMETER_2);
        mConnectedThread.writeByte(DELIMETER_3);

        mConnectedThread.writeByte(ROWS_TOTAL);
        mConnectedThread.writeByte(COLUMNS_TOTAL);

        //block 1,1
        mConnectedThread.writeByte((byte)17);  //00010001
        mConnectedThread.writeByte((byte)15);  //00001111
        mConnectedThread.writeByte((byte)136); //10001000

        //block 1,2
        mConnectedThread.writeByte((byte)18); //00010010
        mConnectedThread.writeByte((byte)15); //00001111
        mConnectedThread.writeByte((byte)17); //00010001

        //block 2,1
        mConnectedThread.writeByte((byte)33); //00100001
        mConnectedThread.writeByte((byte)8);  //00001000
        mConnectedThread.writeByte((byte)143);//10001111

        //block 2,2
        mConnectedThread.writeByte((byte)34); //00100010
        mConnectedThread.writeByte((byte)1);  //00000001
        mConnectedThread.writeByte((byte)31); //00011111
    }
}
