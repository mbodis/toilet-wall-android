package com.svb.toiletwall.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.svb.toiletwall.R;
import com.svb.toiletwall.programs.RandomProgram;
import com.svb.toiletwall.utils.MyShPrefs;

/**
 * Created by mbodis on 9/24/17.
 */

public class ProgramRandomFragment extends ProgramFramgment {

    public static final String TAG = ProgramRandomFragment.class.getName();

    public static ProgramRandomFragment newInstance(Bundle args) {
        ProgramRandomFragment fragment = new ProgramRandomFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_program_random, container, false);

        setupView(rootView);

        return rootView;
    }

    private void setupView(View mView){

    }

    @Override
    void startProgram() {
        program = new RandomProgram(
                MyShPrefs.getBlockCols(getActivity()),
                MyShPrefs.getBlockRows(getActivity()),
                getConnectedThread());
    }
}
