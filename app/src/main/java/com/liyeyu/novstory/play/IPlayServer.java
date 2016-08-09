package com.liyeyu.novstory.play;

import android.support.v4.media.MediaMetadataCompat;

import com.liyeyu.novstory.entry.Audio;

/**
 * Created by Liyeyu on 2016/7/22.
 */
public interface IPlayServer {

    void play();

    void play(Audio audio);

    void play(MediaMetadataCompat curMetadata);

    void pause();

    void stop();

    void seekTo(long pos);

    void skipToNext();

    void skipToPrevious();

    int getCurrentState();

    MediaMetadataCompat getCurrentMetaDate();
}
