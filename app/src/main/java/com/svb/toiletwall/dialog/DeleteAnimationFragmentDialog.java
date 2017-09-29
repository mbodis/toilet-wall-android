package com.svb.toiletwall.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.svb.toiletwall.R;
import com.svb.toiletwall.application.App;
import com.svb.toiletwall.model.db.Animation;
import com.svb.toiletwall.model.db.AnimationDao;
import com.svb.toiletwall.model.db.AnimationFrame;
import com.svb.toiletwall.model.db.DaoSession;

import org.greenrobot.greendao.database.Database;

/**
 * Created by mbodis on 9/26/17.
 */

public class DeleteAnimationFragmentDialog extends DialogFragment {

    public static final String TAG = DeleteAnimationFragmentDialog.class.getName();

    private static final String ARG_ANIMATION_ID = "animationId";

    public static DeleteAnimationFragmentDialog newInstance(long animationIn) {
        DeleteAnimationFragmentDialog frag = new DeleteAnimationFragmentDialog();
        Bundle mBundle = new Bundle();
        mBundle.putLong(ARG_ANIMATION_ID, animationIn);
        frag.setArguments(mBundle);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final long animationId = getArguments().getLong(ARG_ANIMATION_ID, -1);

        final AlertDialog alert = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.dialog_delete_animation_title))
                .setMessage(getString(R.string.dialog_delete_animation_message))
                .setPositiveButton(R.string.dialog_common_cancel, null)
                .setNegativeButton(R.string.dialog_common_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                        DaoSession daoSession = ((App) getActivity().getApplication()).getDaoSession();
                        Database db = daoSession.getDatabase();
                        db.beginTransaction();

                        try {
                            Animation animation = daoSession.getAnimationDao()
                                    .queryBuilder().where(AnimationDao.Properties.Id.eq(animationId)).unique();
                            for (AnimationFrame af : animation.getFrames()){
                                af.delete();
                            }

                            animation.delete();
                            db.setTransactionSuccessful();
                        } catch (Exception ex) {
                            Log.d(TAG, "deleting animation: " + ex.getMessage());
                        } finally {
                            db.endTransaction();
                        }
                        daoSession.clear();

                        Toast.makeText(getActivity(), getString(R.string.dialog_result_msg_deleted), Toast.LENGTH_SHORT).show();
                        getActivity().onBackPressed();
                    }
                })
                .create();
        alert.show();
        return alert;
    }

}
