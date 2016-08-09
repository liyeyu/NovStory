package com.liyeyu.novstory.events;

/**
 * Created by Liyeyu on 2016/7/29.
 */
public class ProgressUpdateEvent {
    public ProgressUpdateEvent progress(int progress) {
        this.progress = progress;
        return this;
    }

    public int getProgress() {
        return progress;
    }

    private int progress;
    public ProgressUpdateEvent() {
    }
}
