package com.svb.toiletwall.programs;

import com.svb.toiletwall.bluetooth.ConnectedThread;

/**
 * Created by mbodis on 10/15/17.
 */

public class TextProgram extends ProgramIface {

    public TextProgram(int cols, int rows, ConnectedThread mConnectedThread) {
        super(cols, rows, mConnectedThread, 500);
    }

    @Override
    synchronized protected void logicExecute() {
        // do nothing, logic is is drawView
    }
}
