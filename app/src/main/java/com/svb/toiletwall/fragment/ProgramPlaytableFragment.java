package com.svb.toiletwall.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.svb.toiletwall.R;
import com.svb.toiletwall.programs.DrawProgram;
import com.svb.toiletwall.support.MyShPrefs;
import com.svb.toiletwall.view.ToiletView;

/**
 * Created by mbodis on 12/10/17.
 */

public class ProgramPlaytableFragment extends ProgramFramgment implements View.OnClickListener{

    public static final String TAG = ProgramPlaytableFragment.class.getName();

    private ToiletView drawView;

    public static ProgramPlaytableFragment newInstance(Bundle args) {
        ProgramPlaytableFragment fragment = new ProgramPlaytableFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_program_playtable, container, false);

        setupView(rootView);

        return rootView;
    }

    private void setupView(View mView){
        drawView = (ToiletView) mView.findViewById(R.id.drawView);
        mView.findViewById(R.id.drawClear).setOnClickListener(this);
        mView.findViewById(R.id.usb).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.drawClear:
                drawView.getToiletDisplay().clearScreen();
                break;

            case R.id.usb:
                //MyUsbDevice.findDeviceAndStartService(getActivity());
//                getActivity().startActivity(new Intent(getActivity(), UsbMidiDriverSampleActivity.class));
                break;
        }
    }

    @Override
    void startProgram() {
        program = new DrawProgram(
                MyShPrefs.getBlockCols(getActivity()),
                MyShPrefs.getBlockRows(getActivity()),
                getConnectedThread());
        drawView.setToiletDisplay(program.getToiletDisplay());
        drawView.startDrawImage();
    }
}

