package com.svb.toiletwall.programs;

import com.svb.toiletwall.bluetooth.ConnectedThread;
import com.svb.toiletwall.bluetooth.ConnectionThreadPool;

/**
 * Created by mbodis on 9/9/17.
 */

public class DrawProgram extends ProgramIface {

    public DrawProgram(int cols, int rows, ConnectionThreadPool mConnectionThreadPool) {
        super(cols, rows, mConnectionThreadPool, 500);
    }

    @Override
    synchronized protected void logicExecute() {
        // do nothing, logic is is drawView
    }
}