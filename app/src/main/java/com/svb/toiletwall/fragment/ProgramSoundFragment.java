package com.svb.toiletwall.fragment;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.svb.toiletwall.R;
import com.svb.toiletwall.model.AudioDataReceivedListener;
import com.svb.toiletwall.model.RecordingThread;
import com.svb.toiletwall.programs.DrawProgram;
import com.svb.toiletwall.utils.MyShPrefs;
import com.svb.toiletwall.view.ToiletView;
import com.svb.toiletwall.view.WaveformView;

/**
 * Created by mbodis on 1/14/18.
 *
 * some docs: https://www.newventuresoftware.com/blog/record-play-and-visualize-raw-audio-data-in-android
 * used lib: https://github.com/newventuresoftware/WaveformControl/
 */

public class ProgramSoundFragment extends ProgramFramgment implements View.OnClickListener{

    public static final String TAG = ProgramSoundFragment.class.getName();

    private static final int REQUEST_RECORD_AUDIO = 13;

    private ToiletView drawView;
    private WaveformView mRealtimeWaveformView;
    private RecordingThread mRecordingThread;

    public static ProgramSoundFragment newInstance(Bundle args) {
        ProgramSoundFragment fragment = new ProgramSoundFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_program_sound, container, false);

        setupView(rootView);
        initRecordThread();

        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();

        mRecordingThread.stopRecording();
    }

    private void setupView(View mView){
        drawView = (ToiletView) mView.findViewById(R.id.drawView);
        mRealtimeWaveformView = (WaveformView) mView.findViewById(R.id.waveformView);
        mView.findViewById(R.id.drawClear).setOnClickListener(this);
        mView.findViewById(R.id.start).setOnClickListener(this);
    }

    private void initRecordThread(){
        mRecordingThread = new RecordingThread(new AudioDataReceivedListener() {
            @Override
            public void onAudioDataReceived(short[] data) {
                Log.d(TAG, "onAudioDataReceived: " + data.length);
                mRealtimeWaveformView.setSamples(data);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.drawClear:
                drawView.getToiletDisplay().clearScreen();
                break;

            case R.id.start:
                if (!mRecordingThread.recording()) {
                    startAudioRecordingSafe();
                } else {
                    mRecordingThread.stopRecording();
                }
                break;
        }
    }

    private void startAudioRecordingSafe() {
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            mRecordingThread.startRecording();
        } else {
            requestMicrophonePermission();
        }
    }

    private void requestMicrophonePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.RECORD_AUDIO)) {
            // Show dialog explaining why we need record audio
            Snackbar.make(mRealtimeWaveformView, "Microphone access is required in order to record audio",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{
                            android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
                }
            }).show();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mRecordingThread.stopRecording();
        }
    }

    @Override
    void startProgram() {
        program = new DrawProgram(
                MyShPrefs.getBlockCols(getActivity()),
                MyShPrefs.getBlockRows(getActivity()),
                getConnectionThreadPool());
        drawView.setToiletDisplay(program.getToiletDisplay());
        drawView.startDrawImage();
    }
}