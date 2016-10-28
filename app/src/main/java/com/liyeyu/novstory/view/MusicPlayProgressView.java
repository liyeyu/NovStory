package com.liyeyu.novstory.view;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.liyeyu.novstory.R;
import com.liyeyu.novstory.play.NovPlayController;

/**底部播放进度条
 * Created by Liyeyu on 2016/7/20.
 */
public class MusicPlayProgressView extends FrameLayout implements SeekBar.OnSeekBarChangeListener {

    private TextView mStart;
    private TextView mEnd;
    private SeekBar mSeekBar;
    PlaybackStateCompat mLastPlaybackState;
    private MediaMetadataCompat mMediaMetadata;
    private int mDuration;
    private long currentMediaId;
    private boolean isTouch;

    public MusicPlayProgressView(Context context) {
        super(context);
        init(context);
    }
    public MusicPlayProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MusicPlayProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.progress_music_play,this);
        mStart = (TextView) inflate.findViewById(R.id.tv_play_time_start);
        mEnd = (TextView) inflate.findViewById(R.id.tv_play_time_end);
        mSeekBar = (SeekBar) inflate.findViewById(R.id.pb_play_progress);
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    public void updateMax(MediaMetadataCompat metadata,PlaybackStateCompat mLastPlaybackState){
        if (metadata == null || mLastPlaybackState == null) {
            return;
        }
        long curId = Long.parseLong(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
        if(curId==currentMediaId){
            return;
        }
        this.mLastPlaybackState = mLastPlaybackState;
        this.mMediaMetadata = metadata;
        currentMediaId = curId;
        mDuration = (int) mMediaMetadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        mSeekBar.setProgress(0);
        mStart.setText(DateUtils.formatElapsedTime(0));
        mSeekBar.setMax(mDuration);
        mEnd.setText(DateUtils.formatElapsedTime(mDuration /1000));
    }

    public void setProgress(int progress){
        if(progress<=0 || progress>=mSeekBar.getMax()){
            progress = 0;
        }
        if(!isTouch){
            mStart.setText(DateUtils.formatElapsedTime(progress/1000));
            mSeekBar.setProgress(progress);
        }
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        mStart.setText(DateUtils.formatElapsedTime(seekBar.getProgress()/1000));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isTouch = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isTouch = false;
        NovPlayController.get().seekTo(seekBar.getProgress());
        mStart.setText(DateUtils.formatElapsedTime(seekBar.getProgress()/1000));
    }
}
