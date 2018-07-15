package com.svb.toiletwall.model.game_logic;

import android.content.Context;

import com.svb.toiletwall.utils.MySupport;

import java.util.Random;

public class Gate {

    private Context ctx;
    private long nextTimeMove = System.currentTimeMillis();
    private long speedMilis = 0;
    private Random rand;
    private boolean gateVisited = false;
    private boolean gameIsRunning = true;

    private int row = -2;
    private int[] rowGate;

    private int score = 0;

    public Gate(Context ctx) {
        this.ctx = ctx;
        this.speedMilis = 500;
        reset();
    }

    private void reset(){
        this.rand = new Random();
        this.speedMilis -= 30;
        if (this.speedMilis < 200){
            this.speedMilis = 200;
        }

        // generate row gate
        this.row = -2;
        rowGate = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        int fromRow = rand.nextInt(rowGate.length);
        for (int i=0; i<(rand.nextInt(3) + 1); i++){
            if (fromRow + i < rowGate.length) {
                rowGate[fromRow + i] = 0;
            }
        }

        this.nextTimeMove = System.currentTimeMillis();
        this.gateVisited = false;
    }

    public void updateMove() {
        if (this.row < 12) {
            if (this.nextTimeMove < System.currentTimeMillis()) {
                this.row++;
                this.nextTimeMove = System.currentTimeMillis() + this.speedMilis;
            }
        }else{
            reset();
        }
    }

    public boolean canBeDraw(){
        return this.getRow() > 0 && this.getRow()<12;
    }

    public int getRow() {
        return this.row;
    }

    public int[] getCols() {
        return rowGate;
    }

    public void throughGate(int rx, int ry) {
        if (!this.gateVisited) {
            if (this.row == ry && this.rowGate[rx]==0) {
                this.gateVisited = true;
                this.score++;
                MySupport.doVibrate(ctx, 40);
            }
        }
    }

    public void detectCollision(int rx, int ry) {
        if (gameIsRunning) {
            if (this.row == ry && rowGate[rx] == 1){
                MySupport.doVibrate(ctx, 500);
                gameIsRunning = false;
            }
        }
    }

    public boolean isGameIsRunning() {
        return gameIsRunning;
    }

    public void setGameIsRunning(boolean gameIsRunning) {
        this.gameIsRunning = gameIsRunning;
    }
}
