package com.svb.toiletwall.model.game_logic;

import java.util.Random;

public class Gate {

    private long nextTimeMove = System.currentTimeMillis();
    private long speedMilis = 0;
    private int typeBlock = -1;
    private int row = -2;
    private Random rand;

    public Gate() {
        this.speedMilis = 500;
        reset();
    }

    private void reset(){
        this.rand = new Random();
        this.speedMilis -= 10;
        if (this.speedMilis <300){
            this.speedMilis = 300;
        }
        this.row = -2;
        this.typeBlock = rand.nextInt(4);
        this.nextTimeMove = System.currentTimeMillis();
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
        int[] arr = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        arr[4 * this.typeBlock] = 0;
        arr[4 * this.typeBlock + 1] = 0;
        arr[4 * this.typeBlock + 2] = 0;
        arr[4 * this.typeBlock + 3] = 0;
        return arr;
    }

    public boolean throughGate(int rx, int ry) {
        // TODO implement
        return false;
    }

    public boolean detectCollision(int row, int col) {
        // TODO implement
        return false;
    }
}
