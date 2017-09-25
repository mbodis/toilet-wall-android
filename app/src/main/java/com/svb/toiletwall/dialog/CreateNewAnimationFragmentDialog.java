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
import com.svb.toiletwall.model.db.DaoSession;


public class CreateNewAnimationFragmentDialog extends DialogFragment {

    public static final String TAG = CreateNewAnimationFragmentDialog.class.getName();

    public static CreateNewAnimationFragmentDialog newInstance() {
        CreateNewAnimationFragmentDialog frag = new CreateNewAnimationFragmentDialog();
        Bundle mBundle = new Bundle();
        frag.setArguments(mBundle);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final View dialogView = getActivity().getLayoutInflater().inflate(
                R.layout.dialog_animation_name, null);

        final EditText animationNameEditText = (EditText) dialogView.findViewById(R.id.animationName);

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
                        Animation animation = new Animation();
                        animation.setName(animationNameEditText.getText().toString());
                        daoSession.getAnimationDao().insert(animation);
                        daoSession.clear();

                        Toast.makeText(getActivity(), "Item created", Toast.LENGTH_SHORT).show();
                    }
                })
                .create();
        alert.show();
        return alert;
    }

}