package com.svb.toiletwall.fragment;

import android.app.Fragment;

import com.svb.toiletwall.activity.MainActivity;
import com.svb.toiletwall.bluetooth.ConnectionThreadPool;
import com.svb.toiletwall.programs.ProgramIface;

/**
 * Created by mbodis on 9/25/17.
 */

abstract class ProgramListFragment extends Fragment {

    protected ProgramIface program;

    abstract void startProgram(int listIdx);

    protected void stopProgram() {
        if (program != null) {
            program.onDestroy();
            program = null;
        }
    }

    @Override
    public void onDestroy() {
        stopProgram();
        super.onDestroy();
    }

    protected ConnectionThreadPool getConnectionThreadPool(){
        return ((MainActivity)getActivity()).getConnectedThreadPool();
    }
}