package com.liyeyu.novstory.view;

import android.view.GestureDetector;
import android.view.MotionEvent;

import com.liyeyu.novstory.events.MusicFlingEvent;
import com.liyeyu.novstory.events.RxBus;

/**
 * Created by Liyeyu on 2016/7/20.
 */
public class FlipperGestureListener implements GestureDetector.OnGestureListener{

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float v, float v1) {

        if(e1==null||e2==null){
            return false;
        }

        if(e2.getX()-e1.getX()>100){
            RxBus.get().post(new MusicFlingEvent(MusicFlingEvent.FLING_RIGHT));
            return true;
        }else if(e2.getX()-e1.getX()<-100){
            RxBus.get().post(new MusicFlingEvent(MusicFlingEvent.FLING_LEFT));
            return true;
        }
        return false;
    }
}
