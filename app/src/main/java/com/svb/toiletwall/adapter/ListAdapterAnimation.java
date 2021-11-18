package com.svb.toiletwall.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.svb.toiletwall.R;
import com.svb.toiletwall.activity.MainActivity;
import com.svb.toiletwall.fragment.ProgramAnimationDetailFragment;
import com.svb.toiletwall.fragment.ProgramAnimationFragment;
import com.svb.toiletwall.model.ToiletDisplay;
import com.svb.toiletwall.model.db.Animation;
import com.svb.toiletwall.programs.AnimationProgram;

import java.util.List;

/**
 * Created by mbodis on 5/23/17.
 */

public class ListAdapterAnimation extends RecyclerView.Adapter<ListAdapterAnimation.ViewHolder> {

    public static final String TAG = ListAdapterAnimation.class.getName();
    public Activity act;
    private ProgramAnimationFragment animFragment;

    public List<Animation> list;
    private int lastPosition = -1;
    private String searchText;

    private AnimationProgram program;


    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public TextView animName, animSize, animFrames;
        public Button playAnimationBtn;

        ViewHolder(View itemView, Context act) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.animationCardView);
            animName = ((TextView) cardView.findViewById(R.id.animationName));
            animSize = ((TextView) cardView.findViewById(R.id.animationSize));
            animFrames = ((TextView) cardView.findViewById(R.id.animationFrames));
            playAnimationBtn = ((Button) cardView.findViewById(R.id.playAnimation));
        }
    }

    public ListAdapterAnimation(Activity act, List<Animation> list, ProgramAnimationFragment animFragment) {
        this.act = act;
        this.list = list;
        this.animFragment = animFragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_list_item_animations, parent, false);
        ViewHolder mViewHolder = new ViewHolder(v, act);
        return mViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int a) {

        final int position = a;
        final Animation mAnimation = list.get(position);

        holder.animName.setText(mAnimation.getName());
        holder.animSize.setText(mAnimation.getCols() * ToiletDisplay.BLOCK_LED_COLS + "x" + mAnimation.getRows() * ToiletDisplay.BLOCK_LED_ROWS
                + " " + act.getString(R.string.animation_adapter_points));
        holder.animFrames.setText(mAnimation.getFrames().size() + " " + act.getString(R.string.animation_adapter_frames));
        holder.playAnimationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animFragment.startProgram(position);
            }
        });


        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = ProgramAnimationDetailFragment.editAnimationBundle(mAnimation.getId());
                ((MainActivity) act).setFragmentAsMain(MainActivity.PAGE_ANIMATION_DETAIL, bundle);
            }
        });

        setAnimation(holder.cardView, position);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    /**
     * Here is the key method to apply the animation
     */
    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            android.view.animation.Animation animation = AnimationUtils.loadAnimation(act, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }
}