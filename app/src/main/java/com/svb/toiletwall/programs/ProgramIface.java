package com.svb.toiletwall.programs;

import com.svb.toiletwall.bluetooth.ConnectionThreadPool;
import com.svb.toiletwall.model.ToiletDisplay;

import static java.lang.Thread.sleep;

/**
 * Created by mbodis on 8/19/17.
 */

public abstract class ProgramIface {

    public static final String TAG = ProgramIface.class.getName();

    // rendering
    private Thread renderThread;
    private boolean renderThreadAlive = true;

    // logic
    private Thread logicThread;
    private boolean logicThreadAlive = true;
    private int logicSleepTime;

    protected ToiletDisplay mToiletDisplay;

    ConnectionThreadPool mConnectionThreadPool;

    ProgramIface(int blockColumns, int blockRows, ConnectionThreadPool mConnectionThreadPool, int logicSleepTime) {
        mToiletDisplay = new ToiletDisplay(blockColumns, blockRows);
        this.mConnectionThreadPool = mConnectionThreadPool;
        this.logicSleepTime = logicSleepTime;

        startRender();
        startLogic();
    }

    private void startRender() {
        renderThread = new Thread(new Runnable() {
            boolean sendFullScreen = false;

            @Override
            public void run() {

                while (renderThreadAlive) {

                    // send 1x full screen
                    if (!sendFullScreen) {
                        sendFullScreen = mToiletDisplay.sendScreenViaBt(mConnectionThreadPool);

                    // send diff
                    }else{
                        mToiletDisplay.sendScreenViaBtPartial(mConnectionThreadPool);
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

    private void startLogic() {
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

    private void stopLogic(){
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
