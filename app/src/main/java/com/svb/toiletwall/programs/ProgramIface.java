package com.svb.toiletwall.programs;

import android.util.Log;

import com.svb.toiletwall.bluetooth.ConnectedThread;
import com.svb.toiletwall.model.ToiletDisplay;

import java.util.Arrays;

import static java.lang.Thread.sleep;

/**
 * Created by mbodis on 8/19/17.
 */

public abstract class ProgramIface {

    public static final String TAG = ProgramIface.class.getName();

    // rendering
    Thread renderThread;
    boolean renderThreadAlive = true;

    // logic
    Thread logicThread;
    boolean logicThreadAlive = true;
    int logicSleepTime;

    ToiletDisplay mToiletDisplay;
    int blockColumns = 0;
    int blockRows = 0;

    boolean lastScreen[][] = null;

    ConnectedThread mConnectedThread;

    ProgramIface(int blockColumns, int blockRows, ConnectedThread mConnectedThread, int logicSleepTime) {
        this.blockColumns = blockColumns;
        this.blockRows = blockRows;
        mToiletDisplay = new ToiletDisplay(blockColumns, blockRows);
        this.mConnectedThread = mConnectedThread;
        this.logicSleepTime = logicSleepTime;

        startRender();
        startLogic();
    }

    public void startRender() {
        renderThread = new Thread(new Runnable() {
            @Override
            public void run() {

                while (renderThreadAlive) {

                    if(hasScreenChanged(mToiletDisplay.getScreen())) {

                        // TODO implement compressed protocol: sends only changed blocks

                        mToiletDisplay.sendScreenViaBt(mConnectedThread);
                        saveScreen(mToiletDisplay);
                    }

                    try {
                        sleep(40); // 25 frame per second == 40 ms wait
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        renderThread.start();
    }

    private void saveScreen(ToiletDisplay mToiletDisplay) {
        this.lastScreen = new boolean[mToiletDisplay.getLedColumns()][mToiletDisplay.getLedRows()];
        for (int c = 0; c < mToiletDisplay.getLedColumns(); c++) {
            for (int r = 0; r < mToiletDisplay.getLedRows(); r++) {
                lastScreen[c][r] = mToiletDisplay.getScreen()[c][r];
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

    private void stopRender(){
        renderThreadAlive = false;
        try {
            if (renderThread.getState() == Thread.State.RUNNABLE) {
                renderThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startLogic() {
        logicThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (logicThreadAlive) {
                    logicExecute();

                    if (logicSleepTime > 0) {
                        try {
                            sleep(logicSleepTime); // 25 frame per second == 40 ms wait
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        logicThread.start();
    }

    protected abstract void logicExecute();

    public void stopLogic(){
        logicThreadAlive = false;
        try {
            if (logicThread.getState() == Thread.State.RUNNABLE) {
                logicThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        stopRender();
        stopLogic();
    }

    public ToiletDisplay getToiletDisplay(){
        return this.mToiletDisplay;
    }
}
