package com.svb.toiletwall.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.svb.toiletwall.R;
import com.svb.toiletwall.programs.DrawProgram;
import com.svb.toiletwall.programs.TextProgram;
import com.svb.toiletwall.support.MyShPrefs;
import com.svb.toiletwall.view.ToiletView;

/**
 * Created by mbodis on 10/15/17.
 */

public class ProgramTextFragment extends ProgramFramgment {

    public static final String TAG = ProgramTextFragment.class.getName();

    private ToiletView drawView;
    private EditText textSpeedET, textSizeET, textMessageET;

    boolean canvasInitialized = false;
    Bitmap bmp;
    Canvas canvas;
    int canvasWidth = 100, canvasHeight = 100;
    Paint black, white;

    int textOffsetY = -2; // TODO ?
    int textSize = 9; // TODO use input
    int textPosX = 0; // TODO loop position - use speed input


    public static ProgramTextFragment newInstance(Bundle args) {
        ProgramTextFragment fragment = new ProgramTextFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_program_text, container, false);

        setupView(rootView);

        return rootView;
    }

    private void setupView(View mView) {
        drawView = (ToiletView) mView.findViewById(R.id.drawView);

        textSpeedET = (EditText) mView.findViewById(R.id.textSpeedInput);
        textSizeET = (EditText) mView.findViewById(R.id.textSizeInput);
        textMessageET = (EditText) mView.findViewById(R.id.textInput);
    }

    private void initColors() {
        black = new Paint();
        black.setColor(Color.BLACK);
        black.setStrokeWidth(1);
        black.setStyle(Paint.Style.FILL);

        white = new Paint();
        white.setColor(Color.WHITE);
        white.setTextSize(textSize);
    }

    private void initCanvas() {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        bmp = Bitmap.createBitmap(canvasWidth, canvasHeight, conf); // this creates a MUTABLE bitmap
        canvas = new Canvas(bmp);
        canvasInitialized = true;
    }

    private void clearCanvas() {
        if (!canvasInitialized) return;

        canvas.drawRect(new Rect(0, 0, canvasWidth, canvasHeight), black);
    }

    private void drawText() {

        if (!canvasInitialized) return;

        canvas.drawText(textMessageET.getText().toString().trim(), textPosX, textSize + textOffsetY, white);

        for (int r = 0; r < program.getToiletDisplay().getLedRows(); r++) {
            for (int c = 0; c < program.getToiletDisplay().getLedColumns(); c++) {

                int rgb = bmp.getPixel(c, r);
                int red = android.graphics.Color.red(rgb);
                int green = android.graphics.Color.green(rgb);
                int blue = android.graphics.Color.blue(rgb);

                boolean pxOn = !(red == 0 && green == 0 && blue == 0);

                program.getToiletDisplay().setScreenPx(c, r, pxOn);
            }
        }
    }

    @Override
    void startProgram() {
        program = new TextProgram(
                MyShPrefs.getBlockCols(getActivity()),
                MyShPrefs.getBlockRows(getActivity()),
                getConnectedThread()) {
            @Override
            protected synchronized void logicExecute() {
                super.logicExecute();
                clearCanvas();
                drawText();
            }
        };
        drawView.setToiletDisplay(program.getToiletDisplay());
        drawView.startDrawImage();

        initColors();
        initCanvas();
        clearCanvas();
    }

    private void test() {
        initColors();
        int w = 100, h = 100;

        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap bmp = Bitmap.createBitmap(w, h, conf); // this creates a MUTABLE bitmap
        Canvas canvas = new Canvas(bmp);
        canvas.drawRect(new Rect(0, 0, w, h), black);
        canvas.drawText("test", 0, textSize - 3, white);
        for (int r = 0; r < 100; r++) {
            String d = "";
            for (int c = 0; c < 100; c++) {

                int rgb = bmp.getPixel(c, r);
                int red = android.graphics.Color.red(rgb);
                int green = android.graphics.Color.green(rgb);
                int blue = android.graphics.Color.blue(rgb);

                boolean pxOn = !(red == 0 && green == 0 && blue == 0);
                d += pxOn ? "0" : "1";

                if (r < 6 && c < 12) {
                    program.getToiletDisplay().setScreenPx(c, r, pxOn);
                }
            }
            Log.d(TAG, "d:" + d);
        }
    }

}

