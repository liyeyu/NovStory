package com.liyeyu.novstory.adapter.callback;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by Liyeyu on 2016/7/19.
 */
public class MusicItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private onMoveAndSwipedListener mListener;
    private int curPos = 0;
    private int tagPos = 0;
    private int state = ItemTouchHelper.ACTION_STATE_IDLE;
    public MusicItemTouchHelperCallback(onMoveAndSwipedListener listener) {
        mListener = listener;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        //拖拽方向
        int dragFlags = ItemTouchHelper.UP|ItemTouchHelper.DOWN;
        //侧滑方向
        int swipeFlags = ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT;
        return makeMovementFlags(dragFlags,swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        //如果两个item不是一个类型的，我们让他不可以拖拽
        if(viewHolder.getItemViewType()!=target.getItemViewType()||mListener==null){
            return false;
        }
        curPos = viewHolder.getAdapterPosition();
        tagPos = target.getAdapterPosition();
        mListener.onItemMove(viewHolder.getAdapterPosition(),tagPos);
        return false;
    }
    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        if(actionState==ItemTouchHelper.ACTION_STATE_IDLE){
            if(state==ItemTouchHelper.ACTION_STATE_DRAG){
                mListener.onItemMoveEnd(curPos,tagPos);
            }
        }
        state = actionState;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        if(mListener==null){
            return ;
        }
        mListener.onItemDismiss(viewHolder.getAdapterPosition());
    }
}
