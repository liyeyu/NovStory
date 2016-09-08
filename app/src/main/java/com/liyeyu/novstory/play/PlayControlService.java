package com.liyeyu.novstory.play;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.liyeyu.novstory.Constants;
import com.liyeyu.novstory.entry.Audio;
import com.liyeyu.novstory.events.MusicChangeEvent;
import com.liyeyu.novstory.events.PlayStateChangeEvent;
import com.liyeyu.novstory.events.ProgressUpdateEvent;
import com.liyeyu.novstory.events.RxBus;
import com.liyeyu.novstory.manager.AppConfig;
import com.liyeyu.novstory.play.callback.MediaControllerCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import liyeyu.support.utils.utils.LogUtil;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class PlayControlService extends Service implements IPlayServer, MediaPlayer.OnCompletionListener {
    public static final float VOLUME_DUCK = 0.2f;
    public static final float VOLUME_NORMAL = 1.0f;
    public static final long PLAYBACK_ACTIONS = PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE |
            PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID | PlaybackStateCompat.ACTION_PAUSE |
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;

    private MediaSessionCompat mMediaSession;
    private MediaSessionCompat.Token token;
    private MediaControllerCompat mediaController = null;
    private MediaControllerCompat.TransportControls mTransportControls;
    private MediaControllerCallback mMediaControllerCallback;
    private AudioManager mAudioManager;
    private ComponentName mRemoteControlReceiverName;
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;
    private MediaPlayer mMediaPlayer;
    private MediaQueueManager mQueueManager;
    private PlaybackStateCompat.Builder mBob;
    private PlayNotificationManager mNotificationManager;
    private MediaSessionCallback mCallback;
    private MusicChangeEvent mChangeEvent;
    private Observable<Integer> mObservable;
    private Subscription mSubscribe;
    private ProgressUpdateEvent mProgressUpdateEvent;
    private boolean isPlayConfig = false;
    private int mProgress;
    private Random random = new Random();
    private List<Integer> randomList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        initPlayController();
    }

    private void initPlayController() {
        NovPlayController.get().initControl(this);
        initMediaPlayer();
        initProgressHandler();
        mNotificationManager = new PlayNotificationManager(this);
        mChangeEvent = new MusicChangeEvent(MusicChangeEvent.ERROR_POS,new PlayStateChangeEvent());
    }


    protected void initProgressHandler(){
        mProgressUpdateEvent = new ProgressUpdateEvent();
        mObservable = Observable.just(0)
                .interval(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .map(new Func1<Long, Integer>() {
                    @Override
                    public Integer call(Long aLong) {
                        return aLong.intValue();
                    }
                });
        updateProgressHandler();
    }

    private synchronized void updateProgressHandler(){
        if(mediaController!=null){
                mSubscribe = mObservable.subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        RxBus.get().post(mProgressUpdateEvent.progress(getProgress()));
                    }
                });
        }
    }
    public void cancelTimer(){
        if(mSubscribe!=null){
            mSubscribe.unsubscribe();
        }
    }

    public int getProgress(){
        return getCurrentStreamPosition();
    }

    private void initMediaPlayer() {
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mRemoteControlReceiverName = new ComponentName(this, RemoteControlReceiver.class);
        //创建会话
        mMediaSession = new MediaSessionCompat(this, getPackageName(),mRemoteControlReceiverName,null);
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mCallback = new MediaSessionCallback();
        mMediaSession.setCallback(mCallback);
        //获得令牌
        token = mMediaSession.getSessionToken();
        try {
            //媒体播放控制器
            mediaController = new MediaControllerCompat(this, token);
            mMediaControllerCallback = new MediaControllerCallback();
            mediaController.registerCallback(mMediaControllerCallback);
            mTransportControls = mediaController.getTransportControls();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    pause();
                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                    if (mMediaPlayer != null) {
                        mMediaPlayer.setVolume(VOLUME_NORMAL, VOLUME_NORMAL); // we can be loud again
                    }
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    if (getCurrentState() == PlaybackStateCompat.STATE_PLAYING
                            ||getCurrentState()==PlaybackStateCompat.STATE_PAUSED) {
                        stop();
                    }
                    mAudioManager.unregisterMediaButtonEventReceiver(mRemoteControlReceiverName);
                    mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                    if (mMediaPlayer != null) {
                        mMediaPlayer.setVolume(VOLUME_DUCK, VOLUME_DUCK);
                    }
                }
            }
        };
        requestAudioFocus();
        mQueueManager = MediaQueueManager.get();
        mBob = new PlaybackStateCompat.Builder();
        publishState(PlaybackState.STATE_STOPPED);
    }

    @Override
    public int getCurrentState(){
        PlaybackStateCompat state = mediaController.getPlaybackState();
        return state.getState();
    }
    @Override
    public MediaMetadataCompat getCurrentMetaDate(){
        return mQueueManager==null?null:mQueueManager.getCurrentMetaData();
    }
    public void setConfigMetaDate(){
        if(mQueueManager==null
                ||getCurrentState()==PlaybackState.STATE_PLAYING
                ||getCurrentState()==PlaybackState.STATE_PAUSED){
            return;
        }
        String mediaId = AppConfig.get(Constants.MEDIA_ID, "");
        LogUtil.i("mediaId:"+mediaId);
        if(!TextUtils.isEmpty(mediaId) && NovPlayController.get().getSession()!=null){
            MediaMetadataCompat metaData = mQueueManager.getMetaData(Long.valueOf(mediaId));
            if(metaData!=null){
                mProgress = AppConfig.get(Constants.LAST_PROGRESS, 0);
                mMediaSession.setMetadata(metaData);
                mediaController.getTransportControls().playFromMediaId(mediaId,null);
                isPlayConfig = true;
                stop();
            }
        }
    }

    public MediaSessionCompat getSession() {
        return mMediaSession;
    }

    public MediaControllerCompat getMediaController() {
        return mediaController;
    }

    public MediaControllerCompat.TransportControls getTransportControls() {
        return mTransportControls;
    }

    public void release() {
        if (mMediaSession != null) {
            mMediaSession.release();
        }
        mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        mAudioManager.unregisterMediaButtonEventReceiver(mRemoteControlReceiverName);
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    public int getCurrentStreamPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    public long getCurrentMediaId() {
        return getMediaId(getCurrentMetaDate());
    }

    public int getSkipQueuePosition(int amount) {
        return mQueueManager==null?-1:mQueueManager.getSkipQueuePosition(amount);
    }
    public MediaMetadataCompat getMediaData(int index) {
        return mQueueManager==null?null:mQueueManager.getMetaData(index);
    }
    public MediaMetadataCompat getMediaData(long id) {
        return mQueueManager==null?null:mQueueManager.getMetaData(id);
    }

    public void updateQueue(int fromPosition, int toPosition) {
        mQueueManager.updateQueue(fromPosition,toPosition);
    }
    public void remove(int position) {
        boolean isCur = false;
        MediaMetadataCompat metaData = mQueueManager.getMetaData(position);
        if(getMediaId(metaData)==getMediaId(getCurrentMetaDate())){
            stop();
            isCur = true;
            mMediaSession.setMetadata(null);
            RxBus.get().post(new MusicChangeEvent(MusicChangeEvent.ERROR_POS).remove(true));
        }
        mQueueManager.remove(position,isCur);
    }

    public long getMediaId(MediaMetadataCompat metaData) {
        if(metaData==null){
            return -1;
        }
        return Long.parseLong(metaData.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
    }

    public void requestAudioFocus() {
        int focus = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (focus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mAudioManager.registerMediaButtonEventReceiver(mRemoteControlReceiverName);
        }
    }

    public void publishState(int state) {
        mBob.setActions(PLAYBACK_ACTIONS);
        switch (state) {
            case PlaybackStateCompat.STATE_PLAYING:
                mBob.setState(state, -1, 1);
                break;
            default:
                mBob.setState(state,-1, 0);
                break;
        }
        PlaybackStateCompat pbState = mBob.build();
        mMediaSession.setPlaybackState(pbState);
        if (state != PlaybackStateCompat.STATE_STOPPED) {
            mMediaSession.setActive(true);
        } else {
            mMediaSession.setActive(false);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppConfig.save(Constants.LAST_PROGRESS,getProgress());
        release();
        mNotificationManager.release();
        cancelTimer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new IPlayBinder();
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        super.unbindService(conn);
    }

//    public IPlayAidlInterface.Stub mStub = new IPlayAidlInterface.Stub() {
//        @Override
//        public void showNotification() throws RemoteException {
//            mNotificationManager.startNotification();
//        }
//    };

    public class IPlayBinder extends Binder {
        public IPlayBinder() {
        }

        public MediaSessionCompat.Token getPlayServiceToken(){
            return token;
        }

    }


    @Override
    public void play() {
        if(getCurrentState()==PlaybackState.STATE_PAUSED){
            publishState(PlaybackState.STATE_PLAYING);
            mTransportControls.play();
            requestAudioFocus();
            if(mMediaPlayer!=null && !mMediaPlayer.isPlaying()){
                mMediaPlayer.start();
            }
        }else if(getCurrentState()==PlaybackState.STATE_STOPPED){
            publishState(PlaybackState.STATE_PLAYING);
            mTransportControls.play();
            if(isPlayConfig && mediaController.getMetadata()!=null){
                play(mediaController.getMetadata());
                isPlayConfig = false;
            }else{
                play(mediaController.getMetadata());
            }
        }else{
            pause();
        }
    }

    public void play(Audio audio){
        MediaMetadataCompat curMetadata = mediaController.getMetadata();
        if(audio==null && curMetadata==null){
            return;


        }
        long id ;
        if(curMetadata!=null && audio!=null){
            id = Long.parseLong(curMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
            if(audio.getId()==id){
                return;
            }
        }
        if(audio!=null){
            curMetadata = MediaQueueManager.CreateMetaData(audio);
        }
        play(curMetadata);
    }

    @Override
    public void play(final MediaMetadataCompat curMetadata) {
        if(curMetadata==null){
            return;
        }
        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                long id = Long.parseLong(curMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
                requestAudioFocus();
                mQueueManager.updateCurrentPos(id);
                mMediaSession.setMetadata(curMetadata);
                mMediaPlayer.setOnCompletionListener(PlayControlService.this);
                mTransportControls.playFromMediaId(String.valueOf(id),null);
                publishState(PlaybackStateCompat.STATE_PLAYING);
                LogUtil.i("play:" + curMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE)+" mCurrentIndex:"+mQueueManager.mCurrentIndex);
                try {
                    mMediaPlayer.reset();
                    mMediaPlayer.setLooping(false);
                    mMediaPlayer.setDataSource(curMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI));
                    mMediaPlayer.prepareAsync();
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            mMediaPlayer.start();
                            if(isPlayConfig && mProgress!=0){
                                seekTo(mProgress);
                                mProgress = 0;
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        mNotificationManager.startNotification();
                    }
                });
    }

    @Override
    public void pause() {
            publishState(PlaybackState.STATE_PAUSED);
            mTransportControls.pause();
            if(mMediaPlayer!=null && mMediaPlayer.isPlaying()){
                mMediaPlayer.pause();
            }
    }

    @Override
    public void stop() {
        publishState(PlaybackState.STATE_STOPPED);
        mTransportControls.stop();
        if(mMediaPlayer!=null){
            mMediaPlayer.stop();
        }
    }

    @Override
    public void seekTo(long pos) {
        publishState(PlaybackStateCompat.STATE_PLAYING);
        mTransportControls.seekTo(pos);
        if(mMediaPlayer!=null){
            mMediaPlayer.seekTo((int) pos);
        }
    }

    @Override
    public void skipToNext() {
//        int mode = AppConfig.get(Constants.PLAY_MODE, MediaQueueManager.PLAY_MODE_ORDER);
//        if(mode==MediaQueueManager.PLAY_MODE_RANDOM){
//            randomList();
//        }else{
            mQueueManager.skipQueuePosition(1);
            publishState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT);
            mTransportControls.skipToNext();
            if(mMediaPlayer!=null){
                play(getCurrentMetaDate());
            }
//        }
    }

    @Override
    public void skipToPrevious() {
//        int mode = AppConfig.get(Constants.PLAY_MODE, MediaQueueManager.PLAY_MODE_ORDER);
//        if(mode==MediaQueueManager.PLAY_MODE_RANDOM){
//            randomList();
//        }else {
            mQueueManager.skipQueuePosition(-1);
            publishState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS);
            mTransportControls.skipToPrevious();
            if(mMediaPlayer!=null){
                play(getCurrentMetaDate());
            }
//        }
    }


    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(mediaPlayer!=null){
            updateMode();
        }
    }

    private void updateMode(){
        int mode = AppConfig.get(Constants.PLAY_MODE, MediaQueueManager.PLAY_MODE_ORDER);
        switch (mode){
            case MediaQueueManager.PLAY_MODE_ORDER:
            case MediaQueueManager.PLAY_MODE_RANDOM:
                skipToNext();
                break;
//                randomList();
//                break;
            case MediaQueueManager.PLAY_MODE_SINGLE_LOOP:
                stop();
                play();
                break;
            case MediaQueueManager.PLAY_MODE_SINGLE:
                stop();
                mMediaPlayer.reset();
                break;
        }
    }

    public void randomList(){
        int currentIndex = mQueueManager.mCurrentIndex;
        List<MediaSessionCompat.QueueItem> list = mQueueManager.getCurrentPlayQueueItems();
        if(list.size()==1 || list.size()==0){
            return;
        }
        randomList.clear();
        for (int i = 0; i < list.size(); i++) {
            if(i!=currentIndex){
                randomList.add(i);
            }
        }
        int index = random.nextInt(randomList.size());
        currentIndex = randomList.get(index);
        if(currentIndex<list.size()){
            play(MediaQueueManager.metaDataIds.get(list.get(currentIndex).getQueueId()));
        }
    }
    
    public class MediaSessionCallback extends MediaSessionCompat.Callback{
        @Override
        public void onPlay() {
            RxBus.get().post(mChangeEvent.state(PlaybackStateCompat.STATE_PLAYING));
            mNotificationManager.startNotification();
        }

        @Override
        public void onPause() {
            RxBus.get().post(mChangeEvent.state(PlaybackStateCompat.STATE_PAUSED));
            mNotificationManager.startNotification();
        }

        @Override
        public void onStop() {
            RxBus.get().post(mChangeEvent.state(PlaybackStateCompat.STATE_STOPPED));
            mNotificationManager.startNotification();
        }

        @Override
        public void onSeekTo(long pos) {
        }

        @Override
        public void onSkipToNext() {
            RxBus.get().post(mChangeEvent.state(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT));
            mNotificationManager.startNotification();
        }

        @Override
        public void onSkipToPrevious() {
            RxBus.get().post(mChangeEvent.state(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS));
            mNotificationManager.startNotification();
        }

       @Override
       public boolean onMediaButtonEvent(Intent intent) {
           if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
               KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
               int action = event.getAction();
               if(action==KeyEvent.ACTION_UP){
                   if (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE == event.getKeyCode()
                           ||KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()) {
                       play();
                   }else if(KeyEvent.KEYCODE_MEDIA_NEXT == event.getKeyCode()){
                       skipToNext();
                   }else if(KeyEvent.KEYCODE_MEDIA_PREVIOUS == event.getKeyCode()){
                       skipToPrevious();
                   }
               }
               LogUtil.i("onMediaButtonEvent:"+action);
           }
           return super.onMediaButtonEvent(intent);
       }
   }
}
