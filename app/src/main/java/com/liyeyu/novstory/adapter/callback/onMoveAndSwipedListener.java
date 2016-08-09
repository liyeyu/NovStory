package com.liyeyu.novstory.adapter.callback;

/**
 * Created by Liyeyu on 2016/7/19.
 */
public interface onMoveAndSwipedListener {
    boolean onItemMove(int fromPosition , int toPosition);
    void onItemMoveEnd(int fromPosition , int toPosition);
    void onItemDismiss(int position);
}
