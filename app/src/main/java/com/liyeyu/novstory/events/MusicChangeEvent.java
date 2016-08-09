package com.liyeyu.novstory.events;

/**
 * Created by Liyeyu on 2016/7/20.
 */
public class MusicChangeEvent{
    public static final int ERROR_POS =-1;
    public int pos;
    public PlayStateChangeEvent playState;
    public boolean isRemove = false;
    public MusicChangeEvent(int pos) {
        this.pos = pos;
    }
    public MusicChangeEvent(int pos,PlayStateChangeEvent playState) {
        this.playState = playState;
        this.pos = pos;
    }
    public MusicChangeEvent state(int state){
        if(playState!=null){
            playState = playState.state(state);
        }
        return this;
    }
    public int getState(){
        if(playState!=null){
            return playState.state;
        }
        return ERROR_POS;
    }

    public MusicChangeEvent remove(boolean remove) {
        isRemove = remove;
        return this;
    }

}
