package com.liyeyu.novstory.events;

/**
 * Created by Liyeyu on 2016/7/26.
 */
public class PlayStateChangeEvent {
    public int state;
    public PlayStateChangeEvent(int state) {
        this.state = state;
    }
    public PlayStateChangeEvent() {
    }
    public PlayStateChangeEvent state(int state){
        this.state = state;
        return this;
    }
}
