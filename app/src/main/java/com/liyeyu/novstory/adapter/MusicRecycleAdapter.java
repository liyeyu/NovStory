package com.liyeyu.novstory.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.jakewharton.rxbinding.view.RxView;
import com.liyeyu.novstory.Constants;
import com.liyeyu.novstory.R;
import com.liyeyu.novstory.adapter.callback.OnMusicItemClick;
import com.liyeyu.novstory.adapter.callback.onMoveAndSwipedListener;
import com.liyeyu.novstory.entry.Audio;
import com.liyeyu.novstory.play.NovPlayController;
import com.liyeyu.novstory.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import liyeyu.support.utils.utils.LogUtil;
import rx.functions.Action1;

/**
 * Created by Liyeyu on 2016/7/19.
 */
public class MusicRecycleAdapter extends RecyclerView.Adapter<MusicViewHolder> implements onMoveAndSwipedListener {
    public static int SCROLL_STATE_DOWN = 0;
    public static int SCROLL_STATE_UP = 1;
    private  int mScrollState;
    private Context mContext;
    private List<Audio> mMedias = new ArrayList<>();
    private OnMusicItemClick mItemClick;

    public MusicRecycleAdapter(Context context,List<Audio> data) {
        this.mContext = context;
        this.mMedias = data;
    }
    public MusicRecycleAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public MusicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.item_music_list, parent, false);
        return new MusicViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(final MusicViewHolder holder, int position) {
        Audio info = mMedias.get(holder.getLayoutPosition());
        if(mItemClick!=null){
            RxView.clicks(holder.itemView).subscribe(new Action1<Void>() {
                @Override
                public void call(Void aVoid) {
                    mItemClick.onItemClick(holder,holder.getLayoutPosition());
                }
            });
            RxView.clicks(holder.mOptions).subscribe(new Action1<Void>() {
                @Override
                public void call(Void aVoid) {
                    mItemClick.onItemOptionsClick(holder.mOptions,holder.getLayoutPosition());
                }
            });
        }
        updateMusicList(holder,info);
    }

    /**
     * update by list change
     * @param holder
     * @param info
     */
    private void updateMusicList(MusicViewHolder holder, Audio info) {
        holder.mTitle.setText(info.getTitle());
        holder.mSinger.setText(info.getArtist());
        holder.mTime.setText(TimeUtils.computer(info.getDuration(), TimeUnit.MILLISECONDS));
        LogUtil.i(info.getId()+" "+info.getTitle());
    }

    @Override
    public int getItemCount() {
        return mMedias!=null?mMedias.size():0;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public void setScrollState(int scrollState) {
        this.mScrollState = scrollState;
    }

    @Override
    public void onViewAttachedToWindow(MusicViewHolder holder) {
        Animation animation ;
        if (SCROLL_STATE_DOWN==mScrollState){
            animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.anim_load_dowm);
        }else{
            animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.anim_load_up);
        }
        holder.itemView.startAnimation(animation);
    }

    @Override
    public void onViewDetachedFromWindow(MusicViewHolder holder) {
        holder.itemView.clearAnimation();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mMedias,fromPosition,toPosition);
        notifyItemMoved(fromPosition,toPosition);
        return false;
    }

    @Override
    public void onItemMoveEnd(int fromPosition, int toPosition) {
        NovPlayController.get().updateQueue(fromPosition,toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        mMedias.remove(position);
        NovPlayController.get().remove(position);
        notifyItemRemoved(position);
    }

    public void setItemClick(OnMusicItemClick itemClick) {
        this.mItemClick = itemClick;
    }

    public void updateData(List<Audio> data){
        if(data!=null){
            mMedias.clear();
            mMedias.addAll(data);
            notifyDataSetChanged();
        }
    }

    public int getCurrentPage(){
        return getItemCount() / Constants.LIST_ITEM_NUM;
    }


    public List<Audio> getData(){
        return mMedias;
    }
}
