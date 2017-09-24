package com.svb.toiletwall.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.svb.toiletwall.R;
import com.svb.toiletwall.programs.DrawProgram;
import com.svb.toiletwall.support.MyShPrefs;
import com.svb.toiletwall.view.ToiletView;

/**
 * Created by mbodis on 9/24/17.
 */

public class ProgramAnimationFragment extends ProgramFramgment implements View.OnClickListener{

    public static final String TAG = ProgramAnimationFragment.class.getName();

    private ToiletView drawView;

    public static ProgramAnimationFragment newInstance(Bundle args) {
        ProgramAnimationFragment fragment = new ProgramAnimationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_program_animation, container, false);

        setupView(rootView);

        return rootView;
    }

    private void setupView(View mView) {
        drawView = (ToiletView) mView.findViewById(R.id.drawView);

        mView.findViewById(R.id.play).setOnClickListener(this);
        mView.findViewById(R.id.prev).setOnClickListener(this);
        mView.findViewById(R.id.next).setOnClickListener(this);
        mView.findViewById(R.id.newframe).setOnClickListener(this);
        mView.findViewById(R.id.animationClear).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.play:
            case R.id.prev:
            case R.id.next:
            case R.id.animationClear:
            case R.id.newframe:
                Toast.makeText(getActivity(), "TODO", Toast.LENGTH_LONG).show();
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
