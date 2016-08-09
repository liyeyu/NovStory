package com.liyeyu.novstory.events;

/**
 * Created by Liyeyu on 2016/7/20.
 */
public class MusicFlingEvent{
    public static final int FLING_LEFT = 1;
    public static final int FLING_RIGHT= 2;
    public int state;
    public MusicFlingEvent(int state) {
        this.state = state;
    }
}
