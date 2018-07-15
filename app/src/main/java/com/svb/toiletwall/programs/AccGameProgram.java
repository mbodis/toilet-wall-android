package com.svb.toiletwall.programs;

import android.content.Context;

import com.svb.toiletwall.bluetooth.ConnectionThreadPool;
import com.svb.toiletwall.fragment.ProgramAccGameFragment;
import com.svb.toiletwall.model.game_logic.Gate;

public class AccGameProgram extends ProgramIface {

    private int lastPosX = 7;
    private int lastPosY = 5;
    private Gate mGate;
    private Context ctx;

    public AccGameProgram(Context ctx, int cols, int rows, ConnectionThreadPool mConnectionThreadPool) {
        super(cols, rows, mConnectionThreadPool, 50);
        this.ctx = ctx;
        mGate = new Gate(ctx);
    }

    /**
     * Given that I'm holding phone in portrait mode:
     * leftRight == -40 (phone is rotated to left)
     * leftRight == 40 (phone is rotated to right)
     * forwardBackward == -40 (phone is rotated to front)
     * forwardBackward == 40 (phone is rotated to back)
     * <p>
     * (leftRight == 0) and (forwardBackward == 0) should be the center of toilet wall
     */
    public void setAccelerometerPosition(int leftRight, int forwardBackward) {
        lastPosX = Math.max(0, 7 + (leftRight / (ProgramAccGameFragment.SENSOR_THRESHOLD_VALUE / (mToiletDisplay.getLedColumns() / 2))));
        lastPosY = Math.max(0, 5 + (forwardBackward / (ProgramAccGameFragment.SENSOR_THRESHOLD_VALUE / (mToiletDisplay.getLedRows() / 2))));
    }

    public void resetGame(){
        mGate = new Gate(this.ctx);
    }

    @Override
    synchronized protected void logicExecute() {
        mToiletDisplay.clearScreen();

        if (mGate != null && mGate.isGameIsRunning()) {
            // move gate
            mGate.updateMove();

            // draw date
            drawGate();

            // through gate
            mGate.throughGate(lastPosX, lastPosY);

            // detect gate collision
            mGate.detectCollision(lastPosX, lastPosY);

            // my position
            drawMyPosition();
        }
    }

    private void drawMyPosition(){
        // draw point
        mToiletDisplay.setScreenPx(
                Math.min(15, lastPosX),
                Math.min(11, lastPosY),
                true
        );
    }

    private void drawGate(){
        // draw gate
        if (mGate.canBeDraw()) {
            for (int i = 0; i < mToiletDisplay.getLedColumns(); i++) {
                mToiletDisplay.setScreenPx(
                        i,
                        mGate.getRow(),
                        mGate.getCols()[i] == 1
                );
            }
        }

    }
}
