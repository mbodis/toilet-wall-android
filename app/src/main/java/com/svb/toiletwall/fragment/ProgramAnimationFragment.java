package com.svb.toiletwall.fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.svb.toiletwall.R;
import com.svb.toiletwall.adapter.ListAdapterAnimation;
import com.svb.toiletwall.application.App;
import com.svb.toiletwall.dialog.CreateNewAnimationFragmentDialog;
import com.svb.toiletwall.model.db.Animation;
import com.svb.toiletwall.model.db.AnimationDao;
import com.svb.toiletwall.model.db.DaoSession;
import com.svb.toiletwall.programs.AnimationProgram;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * Created by mbodis on 9/24/17.
 */

public class ProgramAnimationFragment extends ProgramListFragment implements View.OnClickListener{

    public static final String TAG = ProgramAnimationDetailFragment.class.getName();
    public static final String ACTION_REFRESH_LIST = TAG + "_refresh_list";


    private View loadingView, emptyList;
    private RecyclerView mRecyclerView;

    private ListAdapterAnimation mAdapter;
    private AsyncRetrieveAnimation mAsyncRetrieveAnimation;

    public static ProgramAnimationDetailFragment newInstance(Bundle args) {
        ProgramAnimationDetailFragment fragment = new ProgramAnimationDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static void reloadAnimationList(Activity act){
        act.sendBroadcast(new Intent(ACTION_REFRESH_LIST));
    }

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                if (intent.getAction().equals(ACTION_REFRESH_LIST)){
                    retrieveListItems();
                }
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_program_animation_list, container, false);
        setupView(rootView);
        showLoading(true);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        retrieveListItems();
        getActivity().registerReceiver(mBroadcastReceiver, new IntentFilter(ACTION_REFRESH_LIST));
    }

    @Override
    public void startProgram(int listIdx) {
        if (program != null && program instanceof AnimationProgram) {
            ((AnimationProgram) program).stopAnimation();
        }
        stopProgram();
        program = new AnimationProgram(getConnectedThread(),
                mAdapter.list.get(listIdx));
        ((AnimationProgram)program).playAnimationLoop();
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }

    private void setupView(View mView) {
        loadingView = mView.findViewById(R.id.loading_view);
        emptyList = mView.findViewById(R.id.no_animations);
        mRecyclerView = (RecyclerView) mView.findViewById(android.R.id.list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mView.findViewById(R.id.newAnimation).setOnClickListener(this);
    }

    private void showLoading(boolean showLoading) {
        if (showLoading) {
            emptyList.setVisibility(View.INVISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);
            loadingView.setVisibility(View.VISIBLE);
        } else {
            loadingView.setVisibility(View.INVISIBLE);
        }
    }

    private void reloadContent(List<Animation> list) {
        if (list.size() == 0) {
            mRecyclerView.setVisibility(View.INVISIBLE);
            emptyList.setVisibility(View.VISIBLE);
            return;
        }

        mRecyclerView.setVisibility(View.VISIBLE);
        emptyList.setVisibility(View.INVISIBLE);
        mAdapter = new ListAdapterAnimation(getActivity(), list, this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    private void retrieveListItems() {
        try {
            if (mAsyncRetrieveAnimation != null && !mAsyncRetrieveAnimation.isCancelled()) {
                mAsyncRetrieveAnimation.cancel(true);
            }
            mAsyncRetrieveAnimation = new AsyncRetrieveAnimation();
            mAsyncRetrieveAnimation.execute();
        } catch (Exception e) {
            Log.e(TAG, "retrieve items failed: " + e.getMessage());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.newAnimation:
                DialogFragment newFragment = CreateNewAnimationFragmentDialog.newInstance();
                newFragment.show(getFragmentManager(), CreateNewAnimationFragmentDialog.TAG);
                break;
        }
    }

    /**
     * async retrieve route list
     */
    private class AsyncRetrieveAnimation extends
            AsyncTask<String, Void, List<Animation>> {

        @Override
        protected List<Animation> doInBackground(
                String... params) {

            DaoSession daoSession = ((App) getActivity().getApplication()).getDaoSession();
            QueryBuilder<Animation> query = daoSession.getAnimationDao().queryBuilder();
            query = query.orderDesc(AnimationDao.Properties.Id);

            return query.build().forCurrentThread().list();
        }

        @Override
        protected void onPostExecute(List<Animation> resultList) {
            reloadContent(resultList);
            showLoading(false);
        }
    }
}
