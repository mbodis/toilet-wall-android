package com.svb.toiletwall.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.svb.toiletwall.R;
import com.svb.toiletwall.application.App;
import com.svb.toiletwall.model.db.Animation;
import com.svb.toiletwall.model.db.AnimationDao;
import com.svb.toiletwall.model.db.DaoSession;

/**
 * Created by mbodis on 9/25/17.
 */

public class EditAnimationNameFragmentDialog extends DialogFragment {

    public static final String TAG = EditAnimationNameFragmentDialog.class.getName();

    private static final String ARG_ANIMATION_ID = "animationId";

    public static EditAnimationNameFragmentDialog newInstance(long animationIn) {
        EditAnimationNameFragmentDialog frag = new EditAnimationNameFragmentDialog();
        Bundle mBundle = new Bundle();
        mBundle.putLong(ARG_ANIMATION_ID, animationIn);
        frag.setArguments(mBundle);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final long animationId = getArguments().getLong(ARG_ANIMATION_ID, -1);

        DaoSession daoSession = ((App)getActivity().getApplication()).getDaoSession();
        Animation animation = daoSession.getAnimationDao()
                .queryBuilder().where(AnimationDao.Properties.Id.eq(animationId)).unique();

        final View dialogView = getActivity().getLayoutInflater().inflate(
                R.layout.dialog_animation_name, null);

        final EditText animationNameEditText = (EditText) dialogView.findViewById(R.id.animationName);
        animationNameEditText.setText(animation.getName());

        final AlertDialog alert = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.animation_dialog_set_name))
                .setView(dialogView)
                .setPositiveButton(R.string.dialog_common_cancel, null)
                .setNegativeButton(R.string.dialog_common_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // verify name length
                        if (animationNameEditText.getText().toString().length() == 0){
                            Toast.makeText(getActivity(), "Name not set", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        DaoSession daoSession = ((App)getActivity().getApplication()).getDaoSession();
                        Animation animation = daoSession.getAnimationDao()
                                .queryBuilder().where(AnimationDao.Properties.Id.eq(animationId)).unique();
                        animation.setName(animationNameEditText.getText().toString());
                        animation.update();
                        daoSession.clear();

                        Toast.makeText(getActivity(), "Item changed", Toast.LENGTH_SHORT).show();
                    }
                })
                .create();
        alert.show();
        return alert;
    }

}