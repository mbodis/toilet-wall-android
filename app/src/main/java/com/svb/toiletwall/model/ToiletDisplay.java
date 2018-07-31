package com.svb.toiletwall.model;

import android.util.Log;

import com.svb.toiletwall.bluetooth.ConnectedThread;
import com.svb.toiletwall.bluetooth.ConnectionThreadPool;
import com.svb.toiletwall.model.db.AnimationFrame;

import java.util.Arrays;

/**
 * Created by mbodis on 8/18/17.
 */

public class ToiletDisplay {

    public static final String TAG = ToiletDisplay.class.getName();

    public static final byte BLOCK_LED_COLS = 4;
    public static final byte BLOCK_LED_ROWS = 3;

    private static byte DELIMETER_1 = (byte) 0;
    private static byte DELIMETER_2 = (byte) 255;
    private static byte DELIMETER_3 = (byte) 255;

    private boolean[][] display;
    private byte ledRows, ledColumns;
    private int blockRows, blockColumns;
    private boolean lastScreen[][] = null;

    public ToiletDisplay(int blockColumns, int blockRows) {
        this.blockColumns = blockColumns;
        this.blockRows = blockRows;
        display = new boolean[BLOCK_LED_COLS * blockColumns][BLOCK_LED_ROWS * blockRows];
        this.ledColumns = (byte) (blockColumns * BLOCK_LED_COLS);
        this.ledRows = (byte) (blockRows * BLOCK_LED_ROWS);
        clearScreen();

//        display[0][0] = true;
//        display[1][0] = true;
//        display[2][0] = true;
//        display[3][0] = true;
//        display[4][0] = true;
//        display[5][0] = true;
//        display[6][0] = true;
//        display[7][0] = true;
//
//        display[0][1] = true;
//        display[7][1] = true;
//        display[0][2] = true;
//        display[7][2] = true;
//        display[0][3] = true;
//        display[7][3] = true;
//        display[0][4] = true;
//        display[7][4] = true;
//
//        display[0][5] = true;
//        display[1][5] = true;
//        display[2][5] = true;
//        display[3][5] = true;
//        display[4][5] = true;
//        display[5][5] = true;
//        display[6][5] = true;
//        display[7][5] = true;
    }

    public void clearScreen() {
        for (int c = 0; c < this.ledColumns; c++) {
            for (int r = 0; r < this.ledRows; r++) {
                display[c][r] = false;
            }
        }
    }

    public int getLedColumns() {
        return ledColumns;
    }

    public int getLedRows() {
        return ledRows;
    }

    public void setScreenPx(int col, int row, boolean value) {
        if (col>=0 && col<ledColumns && row >=0 && row <ledRows) {
            display[col][row] = value;
        }
    }

    public void toggleScreenPx(int col, int row) {
        display[col][row] = !display[col][row];
    }

    public int getDisplayValue(int col, int row) {
        return (display[col][row]) ? 1 : 0;
    }

    synchronized public boolean sendScreenViaBt(ConnectionThreadPool mConnectionThreadPool) {
        if (mConnectionThreadPool == null) return false;

        mConnectionThreadPool.writeByte(DELIMETER_1);
        mConnectionThreadPool.writeByte(DELIMETER_2);
        mConnectionThreadPool.writeByte(DELIMETER_3);

        mConnectionThreadPool.writeByte((byte) (this.ledRows / BLOCK_LED_ROWS));
        mConnectionThreadPool.writeByte((byte) (this.ledColumns / BLOCK_LED_COLS));

        // loop all blocks
        for (int c = 0; c < this.blockColumns; c++) {
            for (int r = 0; r < this.blockRows; r++) {
                sendBlockViaBT(mConnectionThreadPool, c, r);
            }
        }

        return true;
    }

    synchronized public int sendScreenViaBtPartial(ConnectionThreadPool mConnectionThreadPool){
        if (mConnectionThreadPool == null) return -1;

        int blockChanged = 0;

        // loop all blocks
        for (int c = 0; c < this.blockColumns; c++) {
            for (int r = 0; r < this.blockRows; r++) {
                if (hasBlockChanged(c, r)){
                    sendBlockViaBT(mConnectionThreadPool, c, r);
                    blockChanged++;
                }
            }
        }

        if(blockChanged > 0){
            //Log.i(TAG, "blockChanged: " + blockChanged);
            saveLastScreen();
        }
        return blockChanged;
    }

    synchronized private void sendBlockViaBT(ConnectionThreadPool mConnectionThreadPool, int c, int r){
        int cc = c * BLOCK_LED_COLS;
        int rr = r * BLOCK_LED_ROWS;

                /*
                 * first byte for each block
                 * bit 7,6,5,4  == column
                 * bit 3,2,1,0  == row
                 */
        int firstByte = Integer.parseInt(getBinaryValue(c + 1, 4) + getBinaryValue(r + 1, 4), 2);
        //Log.d(TAG, "first byte: " + (byte) firstByte);
        mConnectionThreadPool.writeByte((byte) firstByte);

                /*
                 * second byte for each block
                 * bit 7,6,5,4  == 0000
                 * bit 3,2,1,0  == first row of LED block
                 */
        int secondByte1 = 8 * getDisplayValue(cc, rr)
                + 4 * getDisplayValue(cc + 1, rr)
                + 2 * getDisplayValue(cc + 2, rr)
                + 1 * getDisplayValue(cc + 3, rr);
        //Log.d(TAG, "second byte: " + (byte) secondByte1);
        mConnectionThreadPool.writeByte((byte) secondByte1);

                /*
                 * third byte for each block
                 * bit 7,6,5,4  == second row of LED block
                 * bit 3,2,1,0  == third row of LED block
                 */
        int thirdByte1 = 128 * getDisplayValue(cc, rr + 1)
                + 64 * getDisplayValue(cc + 1, rr + 1)
                + 32 * getDisplayValue(cc + 2, rr + 1)
                + 16 * getDisplayValue(cc + 3, rr + 1);
        int thirdByte2 = 8 * getDisplayValue(cc, rr + 2)
                + 4 * getDisplayValue(cc + 1, rr + 2)
                + 2 * getDisplayValue(cc + 2, rr + 2)
                + 1 * getDisplayValue(cc + 3, rr + 2);
        //Log.d(TAG, "third byte1: " + (byte) (thirdByte1));
        //Log.d(TAG, "third byte2: " + (byte) (thirdByte2));
        mConnectionThreadPool.writeByte((byte) (thirdByte1 + thirdByte2));
    }

    private boolean hasBlockChanged(int c, int r){
        if (lastScreen == null) return true;

        for (int bc = (c * BLOCK_LED_COLS); bc < ((c+1) * BLOCK_LED_COLS); bc++){
            for (int br = (r * BLOCK_LED_ROWS); br < ((r+1) * BLOCK_LED_ROWS); br++){
                if (display[bc][br] != lastScreen[bc][br]){
                    return true;
                }
            }
        }

        return false;
    }

    private static String getBinaryValue(int integerValue, int binLength) {
        String binaryStr = "00000000" + Integer.toBinaryString(integerValue);
        int length = binaryStr.length();
        return (binaryStr.substring(length - 4, length));
    }

    public static boolean getBitAtPosition(int integerValue, int position) {
        String binaryStr = Integer.toBinaryString(integerValue);
        return (binaryStr.charAt(position) == '1');
    }

    /**
     * TESTING - show frame for 2x2 blocks
     *
     * @param mConnectedThread
     */
    public void test(ConnectedThread mConnectedThread) {
        mConnectedThread.writeByte(DELIMETER_1);
        mConnectedThread.writeByte(DELIMETER_2);
        mConnectedThread.writeByte(DELIMETER_3);

        mConnectedThread.writeByte(this.ledRows);
        mConnectedThread.writeByte(this.ledColumns);

        //block 1,1
        mConnectedThread.writeByte((byte) 17);  //00010001
        mConnectedThread.writeByte((byte) 15);  //00001111
        mConnectedThread.writeByte((byte) 136); //10001000

        //block 1,2
        mConnectedThread.writeByte((byte) 18); //00010010
        mConnectedThread.writeByte((byte) 15); //00001111
        mConnectedThread.writeByte((byte) 17); //00010001

        //block 2,1
        mConnectedThread.writeByte((byte) 33); //00100001
        mConnectedThread.writeByte((byte) 8);  //00001000
        mConnectedThread.writeByte((byte) 143);//10001111

        //block 2,2
        mConnectedThread.writeByte((byte) 34); //00100010
        mConnectedThread.writeByte((byte) 1);  //00000001
        mConnectedThread.writeByte((byte) 31); //00011111
    }

    public void setScreenByFrame(AnimationFrame animationFrame) {
        for (int row = 0; row < ledRows; row++) {
            for (int col = 0; col < ledColumns; col++) {
                int from = row * ledColumns + col;
                int to = from + 1;

                // fallback for smaller animations
                if (animationFrame.getContent().length() > from) {
                    setScreenPx(col, row, Integer.parseInt(animationFrame.getContent().substring(from, to)) == 1);
                }
            }
        }
    }

    public String getFrameFromScreen() {
        StringBuilder res = new StringBuilder();
        for (int row = 0; row < ledRows; row++) {
            for (int col = 0; col < ledColumns; col++) {
                res.append(String.valueOf(getDisplayValue(col, row)));
            }
        }

        return res.toString();
    }

    public static String getEmptyScreen(int blockCols, int blockRows) {
        StringBuilder res = new StringBuilder();
        for (int row = 0; row < blockRows * BLOCK_LED_ROWS; row++) {
            for (int col = 0; col < blockCols * BLOCK_LED_COLS; col++) {
                res.append(String.valueOf(0));
            }
        }

        return res.toString();
    }

    public boolean[][] getScreen(){
        return display.clone();
    }

    private void saveLastScreen() {
        this.lastScreen = new boolean[getLedColumns()][getLedRows()];
        for (int c = 0; c < getLedColumns(); c++) {
            for (int r = 0; r < getLedRows(); r++) {
                lastScreen[c][r] = getScreen()[c][r];
            }
        }
    }

    private boolean hasScreenChanged(boolean[][] screen) {
        if (lastScreen == null) return true;

        for (int i = 0; i < screen.length; ++i) {
            if (!Arrays.equals(lastScreen[i], screen[i])){
                return true;
            }
        }
        return false;
    }
}
