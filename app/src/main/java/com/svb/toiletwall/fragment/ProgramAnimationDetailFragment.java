package com.svb.toiletwall.fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.svb.toiletwall.R;
import com.svb.toiletwall.application.App;
import com.svb.toiletwall.dialog.DeleteAnimationFragmentDialog;
import com.svb.toiletwall.dialog.EditAnimationNameFragmentDialog;
import com.svb.toiletwall.model.ToiletDisplay;
import com.svb.toiletwall.model.db.Animation;
import com.svb.toiletwall.model.db.AnimationDao;
import com.svb.toiletwall.model.db.AnimationFrame;
import com.svb.toiletwall.model.db.AnimationFrameDao;
import com.svb.toiletwall.model.db.DaoSession;
import com.svb.toiletwall.programs.AnimationProgram;
import com.svb.toiletwall.support.MyShPrefs;
import com.svb.toiletwall.view.ToiletView;

/**
 * Created by mbodis on 9/25/17.
 */

public class ProgramAnimationDetailFragment extends ProgramFramgment implements View.OnClickListener {

    public static final String TAG = ProgramAnimationFragment.class.getName();
    public static final String ARG_ANIMATION_ID = "animationId";
    public static final String ARG_ANIMATION_NAME = "animationName";

    public static final String ACTION_UPDATE_NAME = "action_update";

    private View loadingView;
    private EditText frameSpeed;
    private ToiletView drawView;
    private TextView titleAnimation, animationPage;

    private AsyncRetrieveAnimation mAsyncRetrieveAnimation;
    private Animation animation;
    private long animationId = -1;
    private int currentFrame = 0;

    DaoSession daoSession;

    public static ProgramAnimationFragment newInstance(Bundle args) {
        ProgramAnimationFragment fragment = new ProgramAnimationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static Bundle editAnimationBundle(long tripId) {
        Bundle bundle = new Bundle();
        bundle.putLong(ARG_ANIMATION_ID, tripId);
        return bundle;
    }

    public static void updateAnimationNameView(Activity act, String name) {
        Intent intent = new Intent();
        intent.putExtra(ARG_ANIMATION_NAME, name);
        act.sendBroadcast(intent);
    }

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                if (intent.getAction().equals(ACTION_UPDATE_NAME)) {
                    String name = intent.getStringExtra(ARG_ANIMATION_NAME);
                    if (name != null) {
                        titleAnimation.setText(name);
                    }
                }
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_program_animation_detail, container, false);

        daoSession = ((App) getActivity().getApplication()).getDaoSession();

        setupView(rootView);
        showLoading(true);
        readArguments();
        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        retrieveListItems();
        getActivity().registerReceiver(mBroadcastReceiver, new IntentFilter(ACTION_UPDATE_NAME));
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }

    private void readArguments() {
        animationId = getArguments().getLong(ARG_ANIMATION_ID, -1);
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

        titleAnimation = (TextView) mView.findViewById(R.id.title);
        titleAnimation.setOnClickListener(this);

        animationPage = (TextView) mView.findViewById(R.id.animationPage);
        frameSpeed = (EditText) mView.findViewById(R.id.speed);

        mView.findViewById(R.id.play).setOnClickListener(this);
        mView.findViewById(R.id.stop).setOnClickListener(this);

        mView.findViewById(R.id.forwardStep).setOnClickListener(this);
        mView.findViewById(R.id.forwardFast).setOnClickListener(this);
        mView.findViewById(R.id.backwardStep).setOnClickListener(this);
        mView.findViewById(R.id.backwardFast).setOnClickListener(this);
        mView.findViewById(R.id.framePlus).setOnClickListener(this);
        mView.findViewById(R.id.frameMinus).setOnClickListener(this);
        mView.findViewById(R.id.animationClear).setOnClickListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_animation_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_delete);
        menuItem.setIcon(new IconicsDrawable(getActivity())
                .colorRes(R.color.actionBarIcons)
                .icon(FontAwesome.Icon.faw_trash).actionBar());

        menuItem = menu.findItem(R.id.action_save_frame);
        menuItem.setIcon(new IconicsDrawable(getActivity())
                .colorRes(R.color.actionBarIcons)
                .icon(FontAwesome.Icon.faw_floppy_o).actionBar());

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_delete:
                DialogFragment newFragment = DeleteAnimationFragmentDialog.newInstance(animation.getId());
                newFragment.show(getFragmentManager(), DeleteAnimationFragmentDialog.TAG);
                break;

            case R.id.action_save_frame:
                saveCurrentFrame();
                Toast.makeText(getActivity(), "frame saved", Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.title:
                if (animation != null) {
                    DialogFragment newFragment = EditAnimationNameFragmentDialog.newInstance(animation.getId());
                    newFragment.show(getFragmentManager(), EditAnimationNameFragmentDialog.TAG);
                }
                break;

            case R.id.play:
                play();
                break;

            case R.id.stop:
                stop();
                break;

            case R.id.forwardStep:
                nextFrame();
                break;

            case R.id.forwardFast:
                lastFrame();
                break;

            case R.id.backwardStep:
                prevFrame();
                break;

            case R.id.backwardFast:
                firstFrame();
                break;

            case R.id.animationClear:
                clearFrame();
                break;

            case R.id.framePlus:
                addFrame();
                break;

            case R.id.frameMinus:
                removeFrame();
                break;

        }
    }

    @Override
    void startProgram() {
        program = new AnimationProgram(
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
        titleAnimation.setText(this.animation.getName());

        updatePagination();
        renderDrawView();
        updateMilis();
    }

    private void updatePagination() {
        animationPage.setText((currentFrame+1) + "/" + animation.getFrames().size());
    }

    private void nextFrame() {
        if (currentFrame < (animation.getFrames().size()-1)) {
            currentFrame = currentFrame + 1;
            updatePagination();
            renderDrawView();
            updateMilis();
        }
    }

    private void prevFrame() {
        if (currentFrame > 0) {
            currentFrame = currentFrame - 1;
            updatePagination();
            renderDrawView();
            updateMilis();
        }
    }

    private void firstFrame() {
        if (currentFrame != 0) {
            currentFrame = 0;
            updatePagination();
            renderDrawView();
            updateMilis();
        }
    }

    private void lastFrame() {
        if (currentFrame != (animation.getFrames().size()-1)) {
            currentFrame = animation.getFrames().size() - 1;
            updatePagination();
            renderDrawView();
            updateMilis();
        }
    }

    private void addFrame() {
        AnimationFrame animationFrame = new AnimationFrame();
        animationFrame.setAnimationId(animation.getId());
        animationFrame.setOrder(animation.getFrames().size()); // first frame is 0
        animationFrame.setPlayMilis(400); // TODO sh prefs
        animationFrame.setContent(ToiletDisplay.getEmptyScreen(animation.getCols(), animation.getRows()));
        daoSession.getAnimationFrameDao().insert(animationFrame);
        daoSession.clear();

        retrieveListItems();
        Toast.makeText(getActivity(), "new frame added", Toast.LENGTH_SHORT).show();
    }

    private void removeFrame() {
        if (animation.getFrames().size() > 1) {
            if (currentFrame == (animation.getFrames().size()-1)){
                currentFrame--;
            }

            animation.getFrames().get(currentFrame).delete();
            daoSession.clear();


            retrieveListItems();
        } else {
            Toast.makeText(getActivity(), "last frame cannot be removed", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCurrentFrame() {
        animation.getFrames().get(currentFrame).setContent(program.getToiletDisplay().getFrameFromScreen());
        animation.getFrames().get(currentFrame).setPlayMilis(Integer.parseInt(frameSpeed.getText().toString()));
        animation.getFrames().get(currentFrame).update();
    }

    /**
     * load frame from db, and show on drawView
     */
    private void renderDrawView() {
        program.getToiletDisplay().setScreenByFrame(animation.getFrames().get(currentFrame));
    }

    private void updateMilis() {
        frameSpeed.setText(animation.getFrames().get(currentFrame).getPlayMilis() + "");
    }

    private void play() {
        ((AnimationProgram) program).setFrames(animation.getFrames());
        ((AnimationProgram) program).playAnimationOnce();
    }

    private void stop() {
        ((AnimationProgram) program).stopAnimation();
    }

    private void clearFrame() {
        program.getToiletDisplay().clearScreen();
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
