package com.liyeyu.novstory.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.util.Pair;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.liyeyu.novstory.Constants;
import com.liyeyu.novstory.R;
import com.liyeyu.novstory.act.MusicPlayActivity;
import com.liyeyu.novstory.play.NovPlayController;
import com.liyeyu.novstory.utils.ImageLoader;
import com.liyeyu.novstory.utils.IntentUtils;

import java.util.concurrent.TimeUnit;

import liyeyu.support.utils.utils.LogUtil;
import rx.functions.Action1;

/** Music Play bottom view
 * Created by Liyeyu on 2016/7/18.
 */
public class MusicPlayView extends FrameLayout implements View.OnClickListener{

    private TextView mTitle;
    private TextView mSinger;
    private ImageView mOptions;
    private ImageView mIcon;
    private ImageView mPlay;
    private ImageView mNext;
    private RelativeLayout mLayout;
    private ProgressBar mProgress;
    private long currentMediaId;

    public MusicPlayView(Context context) {
        super(context);
        initView(context);
    }

    public MusicPlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.play_bottom_bar,this);
        mLayout = (RelativeLayout) view.findViewById(R.id.rl_music_play_bottom);
        mIcon = (ImageView) view.findViewById(R.id.iv_music_icon);
        mTitle = (TextView) view.findViewById(R.id.tv_music_item_title);
        mSinger = (TextView) view.findViewById(R.id.tv_music_item_singer);
        mPlay = (ImageView) view.findViewById(R.id.tv_music_play);
        mNext = (ImageView) view.findViewById(R.id.tv_music_play_next);
        mOptions = (ImageView) view.findViewById(R.id.iv_music_play_options);
        mProgress = (ProgressBar) view.findViewById(R.id.pb_music_progress);
        mNext.setOnClickListener(this);
        mPlay.setOnClickListener(this);
        mOptions.setOnClickListener(this);
        RxView.clicks(mLayout).throttleFirst(1, TimeUnit.SECONDS).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                startMusicActivity(new Intent(getContext(), MusicPlayActivity.class));
            }
        });
    }

    public void setTitle(String title){
        mTitle.setText(title);
    }
    public void setSinger(String singer){
        mSinger.setText(singer);
    }
    public void setIcon(long songId,long albumId){
        ImageLoader.get().load(mIcon,songId,albumId);
    }
    public void setIcon(Bitmap bitmap){
        if(bitmap!=null){
            mIcon.setImageBitmap(bitmap);
        }
    }
    public void setPlay(boolean isPause){
       mPlay.setImageResource(isPause?R.drawable.mv_btn_play_prs:R.drawable.mv_btn_pause_prs);
    }
    public void setProgress(int progress){
        if(progress<=0 || progress>=mProgress.getMax()){
            progress = 0;
        }
        mProgress.setProgress(progress);
    }
    public int getProgress(){
        return mProgress.getProgress();
    }

    public void mediaChange(MediaMetadataCompat metadata){
        if (metadata == null) {
            return;
        }
        long curId = Long.parseLong(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
        if(curId==currentMediaId){
            return;
        }
        currentMediaId = curId;
        int max = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        mProgress.setMax(max);
        mProgress.setProgress(0);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_music_play:
                NovPlayController.get().play();
                break;
            case R.id.tv_music_play_next:
                NovPlayController.get().onSkipToNext();
                break;
            case R.id.iv_music_play_options:
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void startMusicActivity(Intent intent){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            Pair<View, String> pair1 = Pair.create((View)getIcon(), getContext().getString(R.string.start_anim_icon));
            Pair<View, String> pair2 = Pair.create((View)getPlay(), getContext().getString(R.string.start_anim_play));
            Pair<View, String> pair3 = Pair.create((View)getTitle(), getContext().getString(R.string.start_anim_title));
            Pair<View, String> pair4 = Pair.create((View)getSinger(), getContext().getString(R.string.start_anim_singer));
            ActivityOptionsCompat optionsCompat
                    = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) getContext(),pair1,pair2,pair3,pair4);
//            optionsCompat.update( ActivityOptionsCompat.makeScaleUpAnimation(
//                    mIcon, mIcon.getWidth()/2, mIcon.getHeight()/2 , mIcon.getWidth(), mIcon.getHeight() ));
            ActivityCompat.startActivityForResult((Activity) getContext(), intent, Constants.LOVE_STATE_CHANGE, optionsCompat.toBundle());
        }else{
            IntentUtils.startPlayActivity((Activity) getContext(),null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        LogUtil.i("play onTouchEvent:"+event.getAction()+"");
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.onTouchEvent(event);
    }

    public TextView getTitle() {
        return mTitle;
    }

    public TextView getSinger() {
        return mSinger;
    }

    public ImageView getIcon() {
        return mIcon;
    }

    public ImageView getPlay() {
        return mPlay;
    }
}
