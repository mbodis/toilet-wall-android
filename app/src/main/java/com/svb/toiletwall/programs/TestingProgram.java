package com.svb.toiletwall.programs;

import com.svb.toiletwall.bluetooth.ConnectedThread;
import com.svb.toiletwall.model.ToiletDisplay;

/**
 * Created by mbodis on 8/19/17.
 */

public class TestingProgram extends ProgramIface{

    int lightRow = 1;

    public TestingProgram(int cols, int rows, ConnectedThread mConnectedThread) {
        super(cols, rows, mConnectedThread, 500);
    }

    @Override
    synchronized protected void logicExecute() {
        lightRow = (lightRow + 1) % (mToiletDisplay.getLedRows());
        mToiletDisplay.clearScreen();
        for (int c = 0; c < mToiletDisplay.getLedColumns(); c++){
            mToiletDisplay.setScreenPx(c, lightRow, true);
        }
    }

}
