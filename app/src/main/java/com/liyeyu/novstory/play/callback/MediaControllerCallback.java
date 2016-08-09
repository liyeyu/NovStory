package com.liyeyu.novstory.play.callback;

import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.liyeyu.novstory.Constants;
import com.liyeyu.novstory.manager.AppConfig;

import java.util.List;

/**
 * Created by Liyeyu on 2016/7/25.
 */
public class MediaControllerCallback extends MediaControllerCompat.Callback {
    @Override
    public void onSessionDestroyed() {
        super.onSessionDestroyed();
    }

    @Override
    public void onSessionEvent(String event, Bundle extras) {
        super.onSessionEvent(event, extras);
    }

    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat state) {
        super.onPlaybackStateChanged(state);
    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        if(metadata!=null){
            AppConfig.save(Constants.MEDIA_ID,Long.parseLong(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID))+"");
        }
//        RxBus.get().post(new MusicChangeEvent(MusicChangeEvent.ERROR_POS,new PlayStateChangeEvent(PlaybackStateCompat.STATE_PLAYING)));
    }

    @Override
    public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
        super.onQueueChanged(queue);
    }

    @Override
    public void onQueueTitleChanged(CharSequence title) {
        super.onQueueTitleChanged(title);
    }
}
