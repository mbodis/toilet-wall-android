package com.svb.toiletwall.programs;

import com.svb.toiletwall.bluetooth.ConnectionThreadPool;

import java.util.ArrayList;

public class SoundProgram extends ProgramIface {

    private ArrayList<Integer> stackVolumeValues = new ArrayList<>();

    public SoundProgram(int cols, int rows, ConnectionThreadPool mConnectionThreadPool) {
        super(cols, rows, mConnectionThreadPool, 50);

        for (int i=0; i< mToiletDisplay.getLedColumns();i ++){
            stackVolumeValues.add(1);
        }
    }

    public void addNewValue(int newVolumeValue){
        stackVolumeValues.add(newVolumeValue);
        stackVolumeValues.remove(0);
    }

    @Override
    synchronized protected void logicExecute() {
        mToiletDisplay.clearScreen();
        for (int x=0; x<mToiletDisplay.getLedColumns(); x++) {
            for (int y=0; y<stackVolumeValues.get(x)/250; y++) {
                mToiletDisplay.setScreenPx(x, Math.max(1, mToiletDisplay.getLedRows()-y),true);
            }
        }
    }
}