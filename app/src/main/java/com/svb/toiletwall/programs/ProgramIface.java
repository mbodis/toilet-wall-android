package com.svb.toiletwall.programs;

import com.svb.toiletwall.bluetooth.ConnectedThread;
import com.svb.toiletwall.model.ToiletDisplay;

import static java.lang.Thread.sleep;

/**
 * Created by mbodis on 8/19/17.
 */

public abstract class ProgramIface {

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
                    mToiletDisplay.sendScreenViaBt(mConnectedThread);

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
                    try {
                        sleep(logicSleepTime); // 25 frame per second == 40 ms wait
                    } catch (InterruptedException e) {
                        e.printStackTrace();
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
