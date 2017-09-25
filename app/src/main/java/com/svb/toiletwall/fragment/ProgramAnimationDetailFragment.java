package com.svb.toiletwall.fragment;

import android.app.DialogFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.svb.toiletwall.R;
import com.svb.toiletwall.application.App;
import com.svb.toiletwall.dialog.EditAnimationNameFragmentDialog;
import com.svb.toiletwall.model.db.Animation;
import com.svb.toiletwall.model.db.AnimationDao;
import com.svb.toiletwall.model.db.DaoSession;
import com.svb.toiletwall.programs.DrawProgram;
import com.svb.toiletwall.support.MyShPrefs;
import com.svb.toiletwall.view.ToiletView;

/**
 * Created by mbodis on 9/25/17.
 */

public class ProgramAnimationDetailFragment extends ProgramFramgment implements View.OnClickListener{

    public static final String TAG = ProgramAnimationFragment.class.getName();
    public static final String KEY_ANIMATION_ID = "animationId";

    private View loadingView;
    private ToiletView drawView;

    private AsyncRetrieveAnimation mAsyncRetrieveAnimation;
    private Animation animation;
    private long animationId = -1;

    public static ProgramAnimationFragment newInstance(Bundle args) {
        ProgramAnimationFragment fragment = new ProgramAnimationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static Bundle editAnimationBundle(long tripId) {
        Bundle bundle = new Bundle();
        bundle.putLong(KEY_ANIMATION_ID, tripId);
        return bundle;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_program_animation_detail, container, false);

        setupView(rootView);
        showLoading(true);
        readArguments();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        retrieveListItems();
    }

    private void readArguments(){
        animationId = getArguments().getLong(KEY_ANIMATION_ID, -1);
    }

    private void showLoading(boolean showLoading) {
        if (showLoading) {
            loadingView.setVisibility(View.VISIBLE);
        } else {
            loadingView.setVisibility(View.INVISIBLE);
        }
    }

    private void setupView(View mView) {
        drawView = (ToiletView) mView.findViewById(R.id.drawView);
        loadingView = mView.findViewById(R.id.loading_view);

        mView.findViewById(R.id.title).setOnClickListener(this);
        mView.findViewById(R.id.play).setOnClickListener(this);
        mView.findViewById(R.id.prev).setOnClickListener(this);
        mView.findViewById(R.id.next).setOnClickListener(this);
        mView.findViewById(R.id.newframe).setOnClickListener(this);
        mView.findViewById(R.id.animationClear).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.title:
                if (animation != null) {
                    DialogFragment newFragment = EditAnimationNameFragmentDialog.newInstance(animation.getId());
                    newFragment.show(getFragmentManager(), EditAnimationNameFragmentDialog.TAG);
                }
                break;

            case R.id.play:
                //TODO implement;
                Toast.makeText(getActivity(), "TODO play", Toast.LENGTH_LONG).show();
                break;

            case R.id.prev:
                //TODO implement;
                Toast.makeText(getActivity(), "TODO prev", Toast.LENGTH_LONG).show();
                break;

            case R.id.next:
                //TODO implement;
                Toast.makeText(getActivity(), "TODO next", Toast.LENGTH_LONG).show();
                break;

            case R.id.animationClear:
                //TODO implement;
                Toast.makeText(getActivity(), "TODO clear", Toast.LENGTH_LONG).show();
                break;

            case R.id.newframe:
                //TODO implement;
                Toast.makeText(getActivity(), "TODO new frame", Toast.LENGTH_LONG).show();
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

    private void retrieveListItems() {
        try {
            if (mAsyncRetrieveAnimation != null && !mAsyncRetrieveAnimation.isCancelled()) {
                mAsyncRetrieveAnimation.cancel(true);
            }
            mAsyncRetrieveAnimation = new AsyncRetrieveAnimation();
            mAsyncRetrieveAnimation.execute(animationId);
        } catch (Exception e) {
            Log.e(TAG, "retrieve items failed: " + e.getMessage());
        }
    }

    private void reloadContent(Animation animation) {
        if (animation == null) {
            Toast.makeText(getActivity(), "unnable to retrieve animation", Toast.LENGTH_SHORT).show();
            getActivity().onBackPressed();
            return;
        }

        this.animation = animation;

        //TODO init content
        // set title
        // set frames
        // set milis
        //...
    }

    /**
     * async retrieve route list
     */
    private class AsyncRetrieveAnimation extends
            AsyncTask<Long, Void, Animation> {

        @Override
        protected Animation doInBackground(
                Long... params) {

            if (params != null && params.length == 1) {

                DaoSession daoSession = ((App) getActivity().getApplication()).getDaoSession();
                return daoSession.getAnimationDao().queryBuilder()
                        .where(AnimationDao.Properties.Id.eq(params[0]))
                        .orderDesc(AnimationDao.Properties.Id)
                        .build().forCurrentThread().listLazy().get(0);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Animation animation) {
            reloadContent(animation);
            showLoading(false);
        }
    }
}
