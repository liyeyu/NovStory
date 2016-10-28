package com.liyeyu.novstory.manager;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.liyeyu.novstory.R;
import com.liyeyu.novstory.events.MusicChangeEvent;
import com.liyeyu.novstory.events.RxBus;
import com.liyeyu.novstory.manager.base.BaseManager;
import com.liyeyu.novstory.play.NovPlayController;
import com.liyeyu.novstory.view.FloatWindow;

import rx.Subscription;
import rx.functions.Action1;

/**
 * Created by Liyeyu on 2016/10/28.
 */

public class NovWindowManager extends BaseManager implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    private static NovWindowManager mManager;
    private SeekBar progress;
    private FloatWindow floatWindow;
    private View menuView, floatView;
    private ImageView imgController, imgCD;
    private ImageButton btnPre, btnPla, btnNex;
    private TextView textDisplay, textPosition, textDuration;
    private boolean userTouch;
    private MediaMetadataCompat mMediaData;
    private Subscription mRxMusicChange;
    private MusicChangeEvent mMusicChangeEvent;
    private ValueAnimator mStartAnimator;
    private ValueAnimator mPasueAnimator;

    private NovWindowManager(Context context) {
        initView(context);
    }

    public static NovWindowManager get(Context context){
        if(mManager==null){
            synchronized (NovWindowManager.class){
                if(mManager==null){
                    mManager = new NovWindowManager(context);
                }
            }
        }
        return mManager;
    }

    private void initView(Context context){
        floatView = LayoutInflater.from(context).inflate(R.layout.window_layout_float, null);
        menuView = LayoutInflater.from(context).inflate(R.layout.window_layout_menu, null);
        btnPre = (ImageButton) menuView.findViewById(R.id.player_previous);
        btnPla = (ImageButton) menuView.findViewById(R.id.player_play);
        btnNex = (ImageButton) menuView.findViewById(R.id.player_next);
        textDuration = (TextView) menuView.findViewById(R.id.player_duration);
        textPosition = (TextView) menuView.findViewById(R.id.player_progress);
        textDisplay = (TextView) menuView.findViewById(R.id.player_title);
        progress = (SeekBar) menuView.findViewById(R.id.player_seek);
        progress.setOnSeekBarChangeListener(this);
        btnPre.setOnClickListener(this);
        btnPla.setOnClickListener(this);
        btnNex.setOnClickListener(this);

        imgCD = (ImageView) floatView.findViewById(R.id.mini_cd);
        imgController = (ImageView) floatView.findViewById(R.id.mini_handle);

        floatWindow = new FloatWindow(context);
        floatWindow.setFloatView(floatView);
        floatWindow.setPlayerView(menuView);

        mRxMusicChange = RxBus.get().register(MusicChangeEvent.class, new Action1<MusicChangeEvent>() {
            @Override
            public void call(MusicChangeEvent changeEvent) {
                if (changeEvent.getState() != MusicChangeEvent.ERROR_POS) {
                    if (changeEvent.getState() != PlaybackStateCompat.STATE_PAUSED) {
                        refresh();
                    }
                    updatePlayView(changeEvent);
                }
            }
        });
    }

    public void release(){
        if(mRxMusicChange!=null){
            mRxMusicChange.unsubscribe();
        }
    }

    private void updatePlayView(MusicChangeEvent changedEvent) {
        mMusicChangeEvent = changedEvent;
        if (changedEvent.getState() == PlaybackStateCompat.STATE_PLAYING
                || changedEvent.getState() == PlaybackStateCompat.STATE_SKIPPING_TO_NEXT
                || changedEvent.getState() == PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS) {
            if(imgController.getDrawable().getLevel()==0){
                startAnimator().start();
            }
            btnPla.setImageResource(R.drawable.landscape_player_btn_pause_normal);
        } else {
            btnPla.setImageResource(R.drawable.landscape_player_btn_play_press);
            pauseAnimator().start();
        }
    }

    public void updateWindow(boolean isCheck){
        if(isCheck){
            floatWindow.show();
        }else{
            floatWindow.dismiss();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        textPosition.setText(DateUtils.formatElapsedTime(NovPlayController.get().getCurrentStreamPosition() /1000));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        userTouch = true;
        pauseAnimator().start();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        userTouch = false;
        NovPlayController.get().seekTo(seekBar.getProgress());
        textPosition.setText(DateUtils.formatElapsedTime(NovPlayController.get().getCurrentStreamPosition() /1000));
        startAnimator().start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.player_previous:
                NovPlayController.get().skipToPrevious();
                break;
            case R.id.player_play:
                NovPlayController.get().play();
                pauseAnimator().start();
                break;
            case R.id.player_next:
                NovPlayController.get().skipToNext();
                break;
            default:
                break;
        }
    }

    /**
     * 开始动画
     * */
    private ValueAnimator startAnimator() {
        mStartAnimator = ValueAnimator.ofInt(0, 10000);
        mStartAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int level = (int) animation.getAnimatedValue();
                imgController.getDrawable().setLevel(level);
            }
        });
        mStartAnimator.setDuration(300);
        return mStartAnimator;
    }

    /**
     * 暂停动画
     * */
    private ValueAnimator pauseAnimator() {
        mPasueAnimator = ValueAnimator.ofInt(10000, 0);
        mPasueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int level = (int) animation.getAnimatedValue();
                imgController.getDrawable().setLevel(level);
            }
        });
        mPasueAnimator.setDuration(300);
        return mPasueAnimator;
    }

    public void attachMediaMetadata(MediaMetadataCompat mediaData){
        mMediaData = mediaData;
        if(mMediaData !=null){
            int duration = (int) mMediaData.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
            String title = mMediaData.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
            String singer = mMediaData.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
            textDisplay.setText(title+" - "+singer);
            progress.setMax(duration);
            textDuration.setText(DateUtils.formatElapsedTime(duration /1000));
        }
    }

    /**
     * 刷新界面
     * */
    public void refresh() {
        if(mMediaData !=null){
            if (mMusicChangeEvent!=null && (mMusicChangeEvent.getState() == PlaybackStateCompat.STATE_PLAYING
                    || mMusicChangeEvent.getState() == PlaybackStateCompat.STATE_SKIPPING_TO_NEXT
                    || mMusicChangeEvent.getState() == PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS)) {
                int level = imgCD.getDrawable().getLevel();
                level = level + 200;
                if(level > 10000) {
                    level = level - 10000;
                }
                imgCD.getDrawable().setLevel(level);
            }
            textPosition.setText(DateUtils.formatElapsedTime(NovPlayController.get().getCurrentStreamPosition() /1000));
            if(!userTouch) {
                progress.setProgress(NovPlayController.get().getCurrentStreamPosition());
            }
        }else{
            btnPla.setImageResource(R.drawable.landscape_player_btn_play_press);
            progress.setSecondaryProgress(0);
            progress.setProgress(0);
            progress.setMax(100);
            textPosition.setText(DateUtils.formatElapsedTime(0));
            textDuration.setText(DateUtils.formatElapsedTime(0));
        }
    }
}
