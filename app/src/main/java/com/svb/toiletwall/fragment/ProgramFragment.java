package com.svb.toiletwall.fragment;

import android.app.Fragment;

import com.svb.toiletwall.activity.MainActivity;
import com.svb.toiletwall.bluetooth.ConnectedThread;
import com.svb.toiletwall.bluetooth.ConnectionThreadPool;
import com.svb.toiletwall.programs.ProgramIface;

/**
 * Created by mbodis on 9/24/17.
 */

public abstract class ProgramFragment extends Fragment {

    protected ProgramIface program;

    abstract void startProgram();

    private void stopProgram() {
        if (program != null) {
            program.onDestroy();
            program = null;
        }
    }

    @Override
    public void onResume() {
        startProgram();
        super.onResume();
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
