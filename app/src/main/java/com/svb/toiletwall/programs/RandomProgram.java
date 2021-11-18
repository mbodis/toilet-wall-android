package com.svb.toiletwall.programs;

import com.svb.toiletwall.bluetooth.ConnectionThreadPool;

import java.util.Random;

/**
 * Created by mbodis on 9/9/17.
 */

public class RandomProgram extends ProgramIface {

    public RandomProgram(int cols, int rows, ConnectionThreadPool mConnectionThreadPool) {
        super(cols, rows, mConnectionThreadPool, 700);
    }

    @Override
    synchronized protected void logicExecute() {
        Random rand = new Random();
        int col = rand.nextInt(mToiletDisplay.getLedColumns());
        int row = rand.nextInt(mToiletDisplay.getLedRows());
        mToiletDisplay.toggleScreenPx(col, row);
    }
}