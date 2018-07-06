package com.svb.toiletwall.fragment;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.svb.toiletwall.R;
import com.svb.toiletwall.programs.TextProgram;
import com.svb.toiletwall.utils.MyShPrefs;
import com.svb.toiletwall.view.ToiletView;

import static java.lang.Thread.sleep;

/**
 * Created by mbodis on 10/15/17.
 */

public class ProgramTextFragment extends ProgramFramgment{

    public static final String TAG = ProgramTextFragment.class.getName();

    // layout
    private ToiletView drawView;
    private EditText textSpeedET, textSizeET, textMessageET;

    // canvas
    private boolean canvasInitialized = false;
    private Bitmap bmp;
    private Canvas canvas;
    private int canvasWidth = 100, canvasHeight = 100;
    private Paint blackPaint, whitePaint;

    // text settings
    private int textOffsetY = -1;
    private int textSize = 9;
    private int textPosX = 0;
    private int textSpeed = 500; // default speed
    private int textWidth = 0;
    String message = "";

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
        textSpeedET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    textSpeed = Integer.parseInt(charSequence.toString());
                    textSettingsChanged();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        textSizeET = (EditText) mView.findViewById(R.id.textSizeInput);
        textSizeET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    textSize = Integer.parseInt(charSequence.toString());
                    textSettingsChanged();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        textMessageET = (EditText) mView.findViewById(R.id.textInput);
        textMessageET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    message = charSequence.toString().trim();
                    textSettingsChanged();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void initColors() {
        blackPaint = new Paint();
        blackPaint.setColor(Color.BLACK);
        blackPaint.setStrokeWidth(1);
        blackPaint.setStyle(Paint.Style.FILL);

        whitePaint = new Paint();
        whitePaint.setColor(Color.WHITE);
        whitePaint.setTextSize(textSize);
    }

    private void initCanvas() {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        bmp = Bitmap.createBitmap(canvasWidth, canvasHeight, conf); // this creates a MUTABLE bitmap
        canvas = new Canvas(bmp);
        canvasInitialized = true;
    }

    synchronized private void textSettingsChanged() {
        // get text length Px
        Rect bounds = new Rect();
        whitePaint.getTextBounds(message, 0, message.length(), bounds);

        textWidth = bounds.width();
        textPosX = program.getToiletDisplay().getLedColumns();//textWidth;
        initColors();
    }

    private void clearCanvas() {
        if (!canvasInitialized) return;
        canvas.drawRect(new Rect(0, 0, canvasWidth, canvasHeight), blackPaint);
    }

    synchronized private void moveLetters() {
        textPosX--;
        if ((textPosX+1) == -textWidth) {
            textPosX = program.getToiletDisplay().getLedColumns();
        }
    }

    private void drawText() {

        if (!canvasInitialized) return;

        canvas.drawText(message, textPosX, textSize + textOffsetY, whitePaint);

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
                getConnectionThreadPool()) {
            @Override
            protected synchronized void logicExecute() {
                super.logicExecute();
                clearCanvas();
                drawText();
                try {
                    sleep(textSpeed);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                moveLetters();
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
        canvas.drawRect(new Rect(0, 0, w, h), blackPaint);
        canvas.drawText("test", 0, textSize - 3, whitePaint);
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

