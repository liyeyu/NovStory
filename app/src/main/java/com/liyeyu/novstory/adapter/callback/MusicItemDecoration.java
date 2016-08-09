package com.liyeyu.novstory.adapter.callback;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Liyeyu on 2016/7/19.
 */
public class MusicItemDecoration extends RecyclerView.ItemDecoration {

    int mSpace;

    /**
     * @param space 传入的值，其单位视为dp
     */
    public MusicItemDecoration(int space) {
        this.mSpace = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = 0;
        outRect.top = 0;
        outRect.bottom = mSpace;
        outRect.right = 0;
    }


}
