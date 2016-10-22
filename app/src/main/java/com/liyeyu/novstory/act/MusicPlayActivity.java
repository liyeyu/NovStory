package com.liyeyu.novstory.act;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.liyeyu.lrcview.lrc.LrcBuilder;
import com.liyeyu.lrcview.lrc.LrcInfo;
import com.liyeyu.lrcview.lrc.LrcView;
import com.liyeyu.novstory.BaseActivity;
import com.liyeyu.novstory.Constants;
import com.liyeyu.novstory.R;
import com.liyeyu.novstory.api.LrcHelper;
import com.liyeyu.novstory.entry.Audio;
import com.liyeyu.novstory.entry.MenuInfo;
import com.liyeyu.novstory.events.MusicChangeEvent;
import com.liyeyu.novstory.events.MusicFlingEvent;
import com.liyeyu.novstory.events.PlayStateChangeEvent;
import com.liyeyu.novstory.events.ProgressUpdateEvent;
import com.liyeyu.novstory.events.RxBus;
import com.liyeyu.novstory.manager.AnimManager;
import com.liyeyu.novstory.manager.AppConfig;
import com.liyeyu.novstory.manager.ShareManager;
import com.liyeyu.novstory.play.MediaQueueManager;
import com.liyeyu.novstory.play.NovPlayController;
import com.liyeyu.novstory.utils.DisplayUtils;
import com.liyeyu.novstory.utils.ImageLoader;
import com.liyeyu.novstory.utils.MediaUtils;
import com.liyeyu.novstory.utils.ToastHelper;
import com.liyeyu.novstory.view.AlbumPicView;
import com.liyeyu.novstory.view.AppBarHeadView;
import com.liyeyu.novstory.view.FlipperGestureListener;
import com.liyeyu.novstory.view.MusicPlayProgressView;
import com.sackcentury.shinebuttonlib.ShineButton;

import java.io.InputStream;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MusicPlayActivity extends BaseActivity implements
        View.OnClickListener, AlbumPicView.onFlingListener {

    private GestureDetector mGestureDetector;
    private AlbumPicView mAlbumPicView;
    private String mTitle;
    private String mSinger;
    private Audio mAudio;
    private ImageView mBackground;
    private LinearLayout mLayoutBottom;
    private ShineButton mLove;
    private MusicPlayProgressView mProgressView;
    private MediaMetadataCompat mMetadata;
    private ImageView mPre;
    private ImageView mNext;
    private ImageView mPlay;
    private Subscription mRxMusicChange;
    private Subscription mRxProgressUpdate;
    private ImageView mMode;
    private long mMediaId;
    private LrcView mLrcView;
    private View mLrcViewMask;
    private String[] mPlayModes;

    @Override
    protected int OnCreateView() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        mRxMusicChange = RxBus.get().register(MusicChangeEvent.class, new Action1<MusicChangeEvent>() {
            @Override
            public void call(MusicChangeEvent changeEvent) {
                if (changeEvent.getState() != MusicChangeEvent.ERROR_POS) {
                    if (changeEvent.getState() != PlaybackStateCompat.STATE_PAUSED) {
                        mediaChange();
                    }
                    updatePlayView(changeEvent);
                }
            }
        });
        mRxProgressUpdate = RxBus.get().register(ProgressUpdateEvent.class, new Action1<ProgressUpdateEvent>() {
            @Override
            public void call(ProgressUpdateEvent progressUpdateEvent) {
                if (mProgressView != null) {
                    mProgressView.setProgress(progressUpdateEvent.getProgress());
                }
                if(mLrcView!=null){
                    mLrcView.setCurrentTimeMillis(progressUpdateEvent.getProgress());
                }
            }
        });
        return R.layout.activity_music_play;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRxMusicChange.unsubscribe();
        mRxProgressUpdate.unsubscribe();
        mAlbumPicView.recycle();
        ActivityCompat.finishAfterTransition(this);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void initView() {
        mGestureDetector = new GestureDetector(this, new FlipperGestureListener());
        mLayoutBottom = (LinearLayout) findViewById(R.id.ll_play_bottom);
        mLove = (ShineButton) findViewById(R.id.iv_play_love);
        mAlbumPicView = (AlbumPicView) findViewById(R.id.apv_album);
        mBackground = (ImageView) findViewById(R.id.iv_play_bg);
        mLrcView = (LrcView) findViewById(R.id.lrc_all);
        mLrcViewMask =  findViewById(R.id.lrc_all_mask);
        mMode = (ImageView) findViewById(R.id.iv_play_mode);
        mPre = (ImageView) findViewById(R.id.iv_play_left);
        mNext = (ImageView) findViewById(R.id.iv_play_next);
        mPlay = (ImageView) findViewById(R.id.iv_play_play);
        mLove.setOnClickListener(this);
        mMode.setOnClickListener(this);
        mPre.setOnClickListener(this);
        mNext.setOnClickListener(this);
        mPlay.setOnClickListener(this);
        mLrcViewMask.setOnClickListener(this);
        mAlbumPicView.setFlingListener(this);
        mProgressView = (MusicPlayProgressView) findViewById(R.id.mv_play_bottom_progress);
        mLayoutBottom.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            boolean update = false;

            @Override
            public void onGlobalLayout() {

                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
                    if (DisplayUtils.getBottomStatusHeight(MusicPlayActivity.this) > 0 && !update) {
                        update = true;
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mLayoutBottom.getLayoutParams();
                        params.setMargins(0, 0, 0, DisplayUtils.getBottomStatusHeight(MusicPlayActivity.this));
                        mLayoutBottom.setLayoutParams(params);
                        mLove.setShineCount(0);
                        mLove.setShineDistanceMultiple(0);
                    }
                }

            }
        });
        mProgressView.updateMax(mMetadata, mMediaController.getPlaybackState());
        mProgressView.setProgress(NovPlayController.get().getCurrentStreamPosition());
        updateAlbumMediaData();
        updatePlayView(new MusicChangeEvent(MusicChangeEvent.ERROR_POS
                , new PlayStateChangeEvent(mMediaController.getPlaybackState().getState())));
        mLove.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean checked) {
                NovPlayController.get().loveSongs(mMediaId,checked);
                setResult(RESULT_OK);
            }
        });
        mPlayModes = getResources().getStringArray(R.array.play_mode);
        initLrcView();
        updatePlayMode(false);
        beginLrcPlay();
    }

    private void initLrcView(){
        mLrcView.setOnPlayerClickListener(new LrcView.OnPlayerClickListener() {
            @Override
            public void onPlayerClicked(long progress, String content) {
                NovPlayController.get().seekTo(progress);
            }
        });
        mLrcView.setOnLrcViewTouchListener(new LrcView.OnLrcClickListener() {
            @Override
            public void onClick(View view, int line, String content) {
                lrcViewAlphaAnim();
            }

            @Override
            public void onLongClick(View view, int line, String content) {
            }
        });
    }
    private void beginLrcPlay(){
        String path = "";
        if(mMetadata!=null){
            path = mMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);
        }else if(mAudio!=null){
            path = mAudio.getPath();
        }
        loadLrcInfo(path,false);
    }

    private void loadLrcInfo(final String path,final boolean end){
        LrcHelper.getFromFile(new LrcBuilder.OnLrcLoadListener() {
            @Override
            public InputStream loadLrc(LrcBuilder builder) {
                return LrcHelper.getStream(path);
            }

            @Override
            public void onLoad(LrcBuilder builder, LrcInfo lrcInfo) {
                if(lrcInfo==null){
                    mLrcView.reset(getString(R.string.lrc_not));
                    if(!end){
                        LrcHelper.getLrcFromUrl(LrcHelper.getCurLrcPath(),mTitle,mSinger,new LrcHelper.OnLrcLoadListener(){

                            @Override
                            public void onSuccess() {
                                loadLrcInfo(LrcHelper.getCurLrcPath(),true);
                            }

                            @Override
                            public void onError() {
                                mLrcView.reset(getString(R.string.lrc_not));
                            }
                        });
                    }
                }else{
                    mLrcView.setLrcInfo(lrcInfo);
                }
            }
        });
    }

    private void updatePlayMode(boolean show) {
        int mode = AppConfig.get(Constants.PLAY_MODE, MediaQueueManager.PLAY_MODE_ORDER);
        if(show)
            ToastHelper.showToast(this,mPlayModes[mode]);
        switch (mode) {
            case MediaQueueManager.PLAY_MODE_ORDER:
                mMode.setImageResource(R.drawable.desk_order);
                break;
            case MediaQueueManager.PLAY_MODE_SINGLE:
                mMode.setImageResource(R.drawable.desk_one);
                break;
            case MediaQueueManager.PLAY_MODE_SINGLE_LOOP:
                mMode.setImageResource(R.drawable.desk_loop);
                break;
            case MediaQueueManager.PLAY_MODE_RANDOM:
                mMode.setImageResource(R.drawable.desk_shuffle);
                break;
        }
    }

    private void updatePlayView(MusicChangeEvent changedEvent) {
        if (changedEvent.getState() == PlaybackStateCompat.STATE_PLAYING
                || changedEvent.getState() == PlaybackStateCompat.STATE_SKIPPING_TO_NEXT
                || changedEvent.getState() == PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS) {
            mPlay.setImageResource(R.drawable.play_btn_pause);
        } else {
            mPlay.setImageResource(R.drawable.play_btn_play);
        }
        mLove.setChecked(NovPlayController.get().isLove(mMediaId));
    }

    private void updateAlbumMediaData() {
        updateAlbum(mMetadata, mAudio, mAlbumPicView.getCurrentIndex());
    }

    private void updateAlbum(MediaMetadataCompat compat, Audio audio, int pos) {
        if (compat != null) {
            updateAlbumPlayView(Long.parseLong(compat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID))
                    , compat.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER), pos);
        } else if (audio != null) {
            updateAlbumPlayView(audio.getId(), audio.getAlbumId(), pos);
        } else {
            mAlbumPicView.getCurrentImageView().setImageResource(ImageLoader.DEF_BG);
        }
    }

    private void updateAlbumPlayView(long songId, long albumId, int pos) {
        mAlbumPicView.setImage(pos,songId,albumId);
        ImageLoader.get().blur(mBackground, MediaUtils.getArtworkUri(this, songId, albumId));
    }


    @Override
    public void showPrevious(int state) {
        if (state == AlbumPicView.ANIM_START) {
            int prePos = NovPlayController.get().getSkipQueuePosition(-1);
            updateMediaData(prePos, null, mAlbumPicView.getPreIndex());
        } else if (state == AlbumPicView.ANIM_END) {
            NovPlayController.get().onSkipToPrevious();
        }
    }

    @Override
    public void showNext(int state) {
        if (state == AlbumPicView.ANIM_START) {
            final int nextPos = NovPlayController.get().getSkipQueuePosition(1);
            updateMediaData(nextPos, null, mAlbumPicView.getNextIndex());
        } else if (state == AlbumPicView.ANIM_END) {
            NovPlayController.get().onSkipToNext();
        }
    }

    private void updateMediaData(final int pos, final Audio audio, final int index) {
        Observable.create(new Observable.OnSubscribe<MediaMetadataCompat>() {
            @Override
            public void call(Subscriber<? super MediaMetadataCompat> subscriber) {
                subscriber.onNext(NovPlayController.get().getMediaData(pos));
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<MediaMetadataCompat>() {
                    @Override
                    public void call(MediaMetadataCompat compat) {
                        updateAlbum(compat, audio, index);
                    }
                });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i("onNewIntent:", "onNewIntent");
        super.onNewIntent(intent);
    }

    @Override
    protected void initData() {
        mAudio = (Audio) getIntent().getSerializableExtra(Constants.TAG);
        mMediaController = NovPlayController.get().getMediaController();
        mMetadata = mMediaController.getMetadata();
        if (mMetadata != null) {
            mMediaId = Long.parseLong(mMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
            mTitle = mMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
            mSinger = mMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        } else if (mAudio != null) {
            mMediaId = mAudio.getId();
            mTitle = mAudio.getTitle();
            mSinger = mAudio.getArtist();
        }
    }

    @Override
    protected boolean isBindPlayService() {
        return true;
    }

    @Override
    protected AppBarHeadView initHeadView(List<MenuInfo> mMenus) {
        mHeadView = (AppBarHeadView) findViewById(R.id.head_music_play);
        mMenus.add(new MenuInfo(R.string.share, getString(R.string.share), R.drawable.actionbar_share));
        mHeadView.setTitle(mTitle);
        mHeadView.setSubTitle(mSinger);
        mHeadView.setHeadBackShow(true);
        removePlayBottom();
        mHeadView.getAppBarLayout().setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
        mHeadView.getToolbar().setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) mHeadView.getToolbar().getLayoutParams();
        params.topMargin = DisplayUtils.getStatusBarHeight(this);
        mHeadView.getToolbar().setLayoutParams(params);
        return mHeadView;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float y = event.getY();
        if (mAlbumPicView!=null && y <= mAlbumPicView.getBottom()) {
            return mGestureDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.string.share) {
            ShareManager.share(mMetadata);
        }
        return super.onOptionsItemSelected(item);
    }

    private void mediaChange() {
        initData();
        updateAlbumMediaData();
        beginLrcPlay();
        mProgressView.updateMax(mMetadata, mMediaController.getPlaybackState());
        mHeadView.setTitle(mTitle);
        mHeadView.setSubTitle(mSinger);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_play_mode:
                NovPlayController.get().nextMode();
                updatePlayMode(true);
                break;
            case R.id.iv_play_left:
                if(mAlbumPicView.getVisibility()==View.VISIBLE){
                    RxBus.get().post(new MusicFlingEvent(MusicFlingEvent.FLING_RIGHT));
                }else{
                    NovPlayController.get().onSkipToPrevious();
                }
                break;
            case R.id.iv_play_play:
                NovPlayController.get().play();
                break;
            case R.id.iv_play_next:
                if(mAlbumPicView.getVisibility()==View.VISIBLE){
                    RxBus.get().post(new MusicFlingEvent(MusicFlingEvent.FLING_LEFT));
                }else{
                    NovPlayController.get().onSkipToNext();
                }
                break;
            case R.id.lrc_all_mask:
                lrcViewAlphaAnim();
                break;
        }
    }

    private void lrcViewAlphaAnim(){
        if(mAlbumPicView.getVisibility()==View.VISIBLE){
            AnimManager.animLrc(1.0f,0f,mAlbumPicView);
            AnimManager.animLrc(1.0f,0f,mLrcViewMask);
            AnimManager.animLrc(0f,1.0f,mLrcView);
        }else{
            AnimManager.animLrc(1.0f,0f,mLrcView);
            AnimManager.animLrc(0f,1.0f,mAlbumPicView);
            AnimManager.animLrc(0f,1.0f,mLrcViewMask);
        }
    }
}
