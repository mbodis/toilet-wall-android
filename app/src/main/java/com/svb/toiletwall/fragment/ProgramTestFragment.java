package com.svb.toiletwall.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.svb.toiletwall.R;
import com.svb.toiletwall.programs.TestingProgram;
import com.svb.toiletwall.support.MyShPrefs;

/**
 * Created by mbodis on 9/24/17.
 */

public class ProgramTestFragment extends ProgramFramgment {

    public static final String TAG = ProgramTestFragment.class.getName();

    public static ProgramTestFragment newInstance(Bundle args) {
        ProgramTestFragment fragment = new ProgramTestFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_program_test, container, false);
        setupView(rootView);

        return rootView;
    }

    private void setupView(View mView) {
        // TODO view assign
    }

    @Override
    void startProgram() {
        program = new TestingProgram(
                MyShPrefs.getBlockCols(getActivity()),
                MyShPrefs.getBlockRows(getActivity()),
                getConnectedThread());
    }
}
