package com.svb.toiletwall.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.svb.toiletwall.model.ToiletDisplay;

/**
 * Created by mbodis on 9/9/17.
 */

public class ToiletView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

    public static final String TAG = ToiletView.class.getName();

    private CanvasThread canvasThread;
    private Paint paintOn, paintOff, paintBg;

    private ToiletDisplay mToiletDisplay;
    private boolean initSizes = false;
    private int sqSize = 0;
    private int topMargin = 0;
    private int leftMargin = 0;

    private int lastCol = -1;
    private int lastRow = -1;

    public ToiletView(Context context) {
        super(context);
    }

    public ToiletView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.getHolder().addCallback(this);
        this.canvasThread = new CanvasThread(getHolder());
        this.setFocusable(true);

        paintBg = new Paint();
        paintBg.setColor(Color.GRAY);

        paintOn = new Paint();
        paintOn.setColor(Color.WHITE);

        paintOff = new Paint();
        paintOff.setColor(Color.BLACK);

        setWillNotDraw(false);
        setOnTouchListener(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        canvasThread.setRunning(false);
        while (retry) {
            try {
                canvasThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    private void initSizes(Canvas canvas) {
        if (!initSizes) {
            int col = mToiletDisplay.getLedColumns();
            int row = mToiletDisplay.getLedRows();

            int widthSqSize = canvas.getWidth() / col;
            int heightSqSize = canvas.getWidth() / row;
            sqSize = ((widthSqSize < heightSqSize) ? widthSqSize : heightSqSize) - 2;

            topMargin = (canvas.getHeight() - (sqSize * row)) / 2;
            leftMargin = (canvas.getWidth() - (sqSize * col)) / 2;

            initSizes = true;
        }
    }

    protected void myDraw(Canvas canvas) {

        initSizes(canvas);
        // draw black bg
        canvas.drawARGB(255, 255, 255, 255);

        // draw
        drawDisp(canvas);

    }

    private void drawDisp(Canvas canvas) {
        for (int c = 0; c < mToiletDisplay.getLedColumns(); c++) {
            for (int r = 0; r < mToiletDisplay.getLedRows(); r++) {

                Rect rectBg = new Rect(
                        leftMargin + c * sqSize, topMargin + r * sqSize,
                        leftMargin + (c + 1) * sqSize, topMargin + (r + 1) * sqSize);

                canvas.drawRect(rectBg, paintBg);

                Rect rect = new Rect(
                        leftMargin + c * sqSize + 1, topMargin + r * sqSize + 1,
                        leftMargin + (c + 1) * sqSize + 1, topMargin + (r + 1) * sqSize + 1);


                if (mToiletDisplay.getDisplayValue(c, r) == 1) {
                    canvas.drawRect(rect, paintOn);
                } else {
                    canvas.drawRect(rect, paintOff);
                }
            }
        }
    }

    public void startDrawImage() {
        canvasThread.setRunning(true);
            canvasThread.start();
    }

    public void setToiletDisplay(ToiletDisplay mToiletDisplay) {
        this.mToiletDisplay = mToiletDisplay;
    }

    public ToiletDisplay getToiletDisplay() {
        return this.mToiletDisplay;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int x = (int) motionEvent.getY();
        int y = (int) motionEvent.getX();

        int newRow = -1;
        int newCol = -1;

        newCol = (y - leftMargin) / sqSize;
        newRow = (x - topMargin) / sqSize;
        //Log.d(TAG, "x: " + x + " y:" + y);
        //Log.d(TAG, "newRow: " + newRow + " newCol:" + newCol);

        if (newRow >= 0 && newRow < mToiletDisplay.getLedRows()
                && newCol >= 0 && newCol < mToiletDisplay.getLedColumns()) {
            if (lastCol != newCol || lastRow != newRow) {
                lastCol = newCol;
                lastRow = newRow;
                mToiletDisplay.toggleScreenPx(lastCol, lastRow);
            }
        }

        if (motionEvent.getAction() == MotionEvent.ACTION_UP){
            lastRow = -1;
            lastCol = -1;
        }

        return true;
    }

    private class CanvasThread extends Thread {
        private SurfaceHolder surfaceHolder;
        private boolean isRun = false;

        public CanvasThread(SurfaceHolder holder) {
            this.surfaceHolder = holder;
        }

        public void setRunning(boolean run) {
            this.isRun = run;
        }

        public boolean isRunning() {
            return this.isRun;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            Canvas c;


            while (isRun) {
                c = null;
                try {
                    c = this.surfaceHolder.lockCanvas(null);
                    if (c != null) {
                        synchronized (this.surfaceHolder) {
                            ToiletView.this.myDraw(c);
                        }
                    }
                } finally {
                    if (c != null)
                        this.surfaceHolder.unlockCanvasAndPost(c);
                }

                try {
                    // 20 redraw times a second
                    sleep(50);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}