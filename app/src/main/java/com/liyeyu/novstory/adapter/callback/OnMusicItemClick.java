package com.liyeyu.novstory.adapter.callback;

import android.view.View;

import com.liyeyu.novstory.adapter.MusicViewHolder;

/**
 * Created by Liyeyu on 2016/7/19.
 */
public interface OnMusicItemClick{
    void onItemClick(MusicViewHolder view, int pos);
    void onItemOptionsClick(View view,int pos);
}
