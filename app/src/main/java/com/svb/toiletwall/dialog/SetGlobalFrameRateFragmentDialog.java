package com.svb.toiletwall.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.svb.toiletwall.R;
import com.svb.toiletwall.application.App;
import com.svb.toiletwall.fragment.ProgramAnimationDetailFragment;
import com.svb.toiletwall.model.db.Animation;
import com.svb.toiletwall.model.db.AnimationDao;
import com.svb.toiletwall.model.db.AnimationFrame;
import com.svb.toiletwall.model.db.DaoSession;
import com.svb.toiletwall.support.MyShPrefs;

import org.greenrobot.greendao.database.Database;

/**
 * Created by mbodis on 10/1/17.
 */

public class SetGlobalFrameRateFragmentDialog extends DialogFragment {

    public static final String TAG = SetGlobalFrameRateFragmentDialog.class.getName();

    private static final String ARG_ANIMATION_ID = "animationId";

    public static SetGlobalFrameRateFragmentDialog newInstance(long animationId) {
        SetGlobalFrameRateFragmentDialog frag = new SetGlobalFrameRateFragmentDialog();
        Bundle mBundle = new Bundle();
        mBundle.putLong(ARG_ANIMATION_ID, animationId);
        frag.setArguments(mBundle);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final long animationId = getArguments().getLong(ARG_ANIMATION_ID, -1);

        final View dialogView = getActivity().getLayoutInflater().inflate(
                R.layout.dialog_global_frame_rate, null);

        final EditText frameRateEditText = (EditText) dialogView.findViewById(R.id.frameRateInput);
        frameRateEditText.setText(MyShPrefs.getFrameDefaultPlayTime(getActivity()) + "");

        final AlertDialog alert = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.dialog_global_frame_rate_title))
                .setView(dialogView)
                .setPositiveButton(R.string.dialog_common_cancel, null)
                .setNegativeButton(R.string.dialog_common_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // verify input length
                        if (frameRateEditText.getText().toString().length() == 0){
                            Toast.makeText(getActivity(), getString(R.string.dialog_animation_invalid_name), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        DaoSession daoSession = ((App)getActivity().getApplication()).getDaoSession();
                        Database db = daoSession.getDatabase();
                        db.beginTransaction();

                        try {
                            Animation animation = daoSession.getAnimationDao().queryBuilder()
                                    .where(AnimationDao.Properties.Id.eq(animationId))
                                    .orderDesc(AnimationDao.Properties.Id)
                                    .build().forCurrentThread().listLazy().get(0);

                            for(AnimationFrame af : animation.getFrames()){
                                af.setPlayMilis(Integer.parseInt(frameRateEditText.getText().toString()));
                                af.update();
                            }

                            db.setTransactionSuccessful();
                        }catch (Exception ex){

                            Log.d(TAG, "update frames milis: " + ex.getMessage());
                        }finally {
                            db.endTransaction();
                        }
                        daoSession.clear();

                        Toast.makeText(getActivity(), getString(R.string.dialog_result_msg_updated), Toast.LENGTH_SHORT).show();

                        ProgramAnimationDetailFragment.reloadItem(getActivity());

                    }
                })
                .create();
        alert.show();
        return alert;
    }

}
