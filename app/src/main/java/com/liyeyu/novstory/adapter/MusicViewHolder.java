package com.liyeyu.novstory.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.liyeyu.novstory.R;

/**
 * Created by Liyeyu on 2016/7/19.
 */
public class MusicViewHolder extends RecyclerView.ViewHolder {
    public TextView mTitle;
    public TextView mSinger;
    public TextView mTime;
    public ImageView mOptions;
    public MusicViewHolder(View itemView) {
        super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.tv_music_item_title);
            mSinger = (TextView) itemView.findViewById(R.id.tv_music_item_singer);
            mTime = (TextView) itemView.findViewById(R.id.tv_music_item_time);
            mOptions = (ImageView) itemView.findViewById(R.id.iv_music_item_options);
    }
}
