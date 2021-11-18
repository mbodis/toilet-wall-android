package com.svb.toiletwall.programs;

import com.svb.toiletwall.bluetooth.ConnectionThreadPool;

/**
 * Created by mbodis on 10/15/17.
 */

public class TextProgram extends ProgramIface {

    public TextProgram(int cols, int rows, ConnectionThreadPool mConnectionThreadPool) {
        super(cols, rows, mConnectionThreadPool, 50);
    }

    @Override
    synchronized protected void logicExecute() {
        // do nothing, logic is is drawView
    }
}
