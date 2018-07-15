package com.svb.toiletwall.fragment;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.svb.toiletwall.R;
import com.svb.toiletwall.programs.AccGameProgram;
import com.svb.toiletwall.programs.DrawProgram;
import com.svb.toiletwall.utils.MyShPrefs;
import com.svb.toiletwall.view.ToiletView;

import java.util.List;

public class ProgramAccGameFragment extends ProgramFramgment implements View.OnClickListener, SensorEventListener {

    public static final String TAG = ProgramAccGameFragment.class.getName();

    // layout
    private ToiletView drawView;

    // sensor
    public static final int SENSOR_THRESHOLD_VALUE = 40;
    private SensorManager mSensorManager;

    public static ProgramAccGameFragment newInstance(Bundle args) {
        ProgramAccGameFragment fragment = new ProgramAccGameFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_program_acc_game, container, false);

        setupView(rootView);
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        registerSensor();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterSensor();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.drawClear:
                drawView.getToiletDisplay().clearScreen();
                break;

            case R.id.resetGame:
                ((AccGameProgram)program).resetGame();
                break;
        }
    }

    @Override
    void startProgram() {
        program = new AccGameProgram(
                getActivity(),
                MyShPrefs.getBlockCols(getActivity()),
                MyShPrefs.getBlockRows(getActivity()),
                getConnectionThreadPool());
        drawView.setToiletDisplay(program.getToiletDisplay());
        drawView.startDrawImage();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // mAccelX = 0 - event.values[2];
        // mAccelY = 0 - event.values[1];
        // mAccelZ = event.values[0];

        if (event == null)
            return;

        int forwBack = (int) (event.values[1]);
        if (forwBack > SENSOR_THRESHOLD_VALUE) {
            forwBack = SENSOR_THRESHOLD_VALUE;
        }
        if (forwBack < -SENSOR_THRESHOLD_VALUE) {
            forwBack = -SENSOR_THRESHOLD_VALUE;
        }

        int lefRig = (int) (event.values[2]);
        if (lefRig > SENSOR_THRESHOLD_VALUE) {
            lefRig = SENSOR_THRESHOLD_VALUE;
        }
        if (lefRig < -SENSOR_THRESHOLD_VALUE) {
            lefRig = -SENSOR_THRESHOLD_VALUE;
        }

        ((AccGameProgram)program).setAccelerometerPosition(-lefRig, -forwBack);
    }

    private void setupView(View mView) {
        drawView = (ToiletView) mView.findViewById(R.id.drawView);
        mView.findViewById(R.id.resetGame).setOnClickListener(this);
    }

    private void registerSensor() {

        // showAllSensors();
        List<Sensor> sensorList = mSensorManager
                .getSensorList(Sensor.TYPE_ORIENTATION);
        mSensorManager.registerListener(this, sensorList.get(0),
                SensorManager.SENSOR_DELAY_GAME);

    }

    private void unregisterSensor() {
        mSensorManager.unregisterListener(this);
    }
}
