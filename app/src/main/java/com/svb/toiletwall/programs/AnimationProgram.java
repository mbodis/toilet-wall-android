package com.svb.toiletwall.programs;

import com.svb.toiletwall.bluetooth.ConnectionThreadPool;
import com.svb.toiletwall.model.db.Animation;
import com.svb.toiletwall.model.db.AnimationFrame;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * Created by mbodis on 9/26/17.
 */

public class AnimationProgram extends ProgramIface {

    private List<AnimationFrame> frames = new ArrayList<AnimationFrame>();
    private int currentFrame = 0;
    private boolean isPlaying = false;

    public AnimationProgram(int cols, int rows, ConnectionThreadPool mConnectionThreadPool) {
        super(cols, rows, mConnectionThreadPool, 0);
    }

    public AnimationProgram(ConnectionThreadPool mConnectionThreadPool, Animation animation) {
        super(animation.getCols(), animation.getRows(), mConnectionThreadPool, 0);
        frames = animation.getFrames();
    }

    @Override
    synchronized protected void logicExecute() {
        // do nothing, logic is is drawView

        if (isPlaying) {
            mToiletDisplay.setScreenByFrame(frames.get(currentFrame));

            try {
                sleep(frames.get(currentFrame).getPlayMilis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            endAnimation();
        }
    }

    public void playAnimationLoop(){
        isPlaying = true;
    }

    public void stopAnimation(){
        isPlaying = false;
    }

    public void setFrames(List<AnimationFrame> frames){
        this.frames = frames;
    }

    /**
     * increase frame
     * check if animation ends
     * clear screen
     */
    private void endAnimation() {
        currentFrame++;
        if (currentFrame >= frames.size()){
            currentFrame = 0;
        }
    }
}