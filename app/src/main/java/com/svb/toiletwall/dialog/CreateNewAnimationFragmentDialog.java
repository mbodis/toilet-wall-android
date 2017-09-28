package com.svb.toiletwall.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.svb.toiletwall.R;
import com.svb.toiletwall.application.App;
import com.svb.toiletwall.fragment.ProgramAnimationFragment;
import com.svb.toiletwall.model.ToiletDisplay;
import com.svb.toiletwall.model.db.Animation;
import com.svb.toiletwall.model.db.AnimationFrame;
import com.svb.toiletwall.model.db.DaoSession;
import com.svb.toiletwall.support.MyShPrefs;

import org.greenrobot.greendao.database.Database;


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
                .setTitle(getString(R.string.dialog_animation_set_name))
                .setView(dialogView)
                .setPositiveButton(R.string.dialog_common_cancel, null)
                .setNegativeButton(R.string.dialog_common_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // verify name length
                        if (animationNameEditText.getText().toString().length() == 0){
                            Toast.makeText(getActivity(), getString(R.string.dialog_animation_invalid_name), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        DaoSession daoSession = ((App)getActivity().getApplication()).getDaoSession();
                        Database db = daoSession.getDatabase();
                        db.beginTransaction();

                        try {
                            Animation animation = new Animation();
                            animation.setName(animationNameEditText.getText().toString());
                            animation.setRows(MyShPrefs.getBlockRows(getActivity()));
                            animation.setCols(MyShPrefs.getBlockCols(getActivity()));
                            daoSession.getAnimationDao().insert(animation);


                            AnimationFrame animationFrame = new AnimationFrame();
                            animationFrame.setAnimationId(animation.getId());
                            animationFrame.setOrder(0);
                            animationFrame.setPlayMilis(400); // TODO sh prefs
                            animationFrame.setContent(ToiletDisplay.getEmptyScreen(animation.getCols(), animation.getRows()));
                            daoSession.getAnimationFrameDao().insert(animationFrame);

                            db.setTransactionSuccessful();
                        }catch (Exception ex){

                            Log.d(TAG, "transaction: " + ex.getMessage());
                        }finally {
                            db.endTransaction();
                        }
                        daoSession.clear();

                        Toast.makeText(getActivity(), getString(R.string.dialog_result_msg_created), Toast.LENGTH_SHORT).show();

                        ProgramAnimationFragment.reloadAnimationList(getActivity());

                    }
                })
                .create();
        alert.show();
        return alert;
    }

}