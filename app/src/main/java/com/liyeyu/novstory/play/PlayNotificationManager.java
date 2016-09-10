package com.liyeyu.novstory.play;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.RemoteViews;

import com.liyeyu.novstory.Constants;
import com.liyeyu.novstory.R;
import com.liyeyu.novstory.act.MusicPlayActivity;
import com.liyeyu.novstory.events.MusicChangeEvent;
import com.liyeyu.novstory.events.RxBus;
import com.liyeyu.novstory.utils.ImageLoader;
import com.liyeyu.novstory.utils.ResourceHelper;

import rx.functions.Action1;

/**
 * Created by Liyeyu on 2016/7/21.
 */
public class PlayNotificationManager extends BroadcastReceiver implements Action1<MusicChangeEvent> {
    public static final String ACTION_PAUSE = Constants.PACKAGE_NAME +".pause";
    public static final String ACTION_PLAY = Constants.PACKAGE_NAME +".play";
    public static final String ACTION_PREV = Constants.PACKAGE_NAME +".prev";
    public static final String ACTION_NEXT = Constants.PACKAGE_NAME +".next";
    public static final String ACTION_STOP = Constants.PACKAGE_NAME +".stop";
    public static final String ACTION_CLEAR = Constants.PACKAGE_NAME +".clear";
    public static final int NOTIFICATION_ID = 1129;
    private static final int REQUEST_CODE = 100;
    private final PendingIntent mPauseIntent;
    private final PendingIntent mPlayIntent;
    private final PendingIntent mPreviousIntent;
    private final PendingIntent mNextIntent;
    private final PendingIntent mStopIntent;
    private final int mNotificationColor;
    private final NotificationManagerCompat mNotificationManager;
    private final PlayControlService mService;
    private final MediaControllerCompat mController;
    private final IntentFilter mIntentFilter;
    private final PendingIntent mClearIntent;
    private MediaMetadataCompat mMetadata;
    private int mPlaybackState;
    private Notification mNotification;
    private NotificationCompat.Builder mBuilder;
    private String mAction;


    public PlayNotificationManager(PlayControlService service){
        RxBus.get().register(MusicChangeEvent.class,this);
        mService = service;
        mNotificationColor = ResourceHelper.getThemeColor(mService, R.attr.colorPrimary,
                ContextCompat.getColor(service,R.color.gray_f5));
        mNotificationManager = NotificationManagerCompat.from(service);
        mController = mService.getMediaController();

        String pkg = mService.getPackageName();
        mPauseIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PAUSE).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPlayIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PLAY).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPreviousIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PREV).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mNextIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_NEXT).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mStopIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_STOP).setPackage(pkg),
                PendingIntent.FLAG_CANCEL_CURRENT);
        mClearIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_CLEAR).setPackage(pkg),
                PendingIntent.FLAG_CANCEL_CURRENT);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(ACTION_NEXT);
        mIntentFilter.addAction(ACTION_PAUSE);
        mIntentFilter.addAction(ACTION_PLAY);
        mIntentFilter.addAction(ACTION_PREV);
        mIntentFilter.addAction(ACTION_STOP);
        mIntentFilter.addAction(ACTION_CLEAR);
        mService.registerReceiver(this, mIntentFilter);
    }

    // 通知栏显示当前播放信息，利用通知和 PendingIntent来启动对应的activity
    private Notification createNotification() {

        Intent intent = new Intent(mService, MusicPlayActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mBuilder = new NotificationCompat.Builder(
                mService);
        //兼容 6.0通知栏样式
        PendingIntent contentIntent = PendingIntent.getActivity(mService, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String mTitle = mMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
        String mSinger = mMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        long mSongId = Long.parseLong(mMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
        long mAlbumId = mMetadata.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER);
        Bitmap mAlbumBitmap = ImageLoader.get().load(mSongId, mAlbumId);
        RemoteViews bigViews = new RemoteViews(mService.getPackageName(), R.layout.notification_play);
        bigViews.setImageViewBitmap(R.id.iv_music_icon, mAlbumBitmap);
        bigViews.setTextViewText(R.id.tv_notify_title,mTitle);
        bigViews.setTextViewText(R.id.tv_notify_singer, mSinger);
        bigViews.setOnClickPendingIntent(R.id.iv_notify_close, mClearIntent);
        bigViews.setOnClickPendingIntent(R.id.iv_notify_play_pre,mPreviousIntent);
        bigViews.setOnClickPendingIntent(R.id.tv_notify_play_next,mNextIntent);
        if(mPlaybackState==PlaybackStateCompat.STATE_PAUSED
                || mPlaybackState==PlaybackStateCompat.STATE_STOPPED){
            bigViews.setOnClickPendingIntent(R.id.tv_notify_play,mPlayIntent);
            bigViews.setImageViewResource(R.id.tv_notify_play,R.drawable.mv_btn_play_prs);
        }else{
            bigViews.setOnClickPendingIntent(R.id.tv_notify_play,mPauseIntent);
            bigViews.setImageViewResource(R.id.tv_notify_play,R.drawable.mv_btn_pause_prs);
        }
        RemoteViews customViews = new RemoteViews(mService.getPackageName(), R.layout.notification_play_custom);
        customViews.setImageViewBitmap(R.id.iv_music_icon, mAlbumBitmap);
        customViews.setTextViewText(R.id.tv_notify_title,mTitle);
        customViews.setTextViewText(R.id.tv_notify_singer, mSinger);
        customViews.setOnClickPendingIntent(R.id.iv_notify_close1, mClearIntent);
        customViews.setOnClickPendingIntent(R.id.iv_notify_play_pre1,mPreviousIntent);
        customViews.setOnClickPendingIntent(R.id.tv_notify_play_next1,mNextIntent);
        if(mPlaybackState==PlaybackStateCompat.STATE_PAUSED
                || mPlaybackState==PlaybackStateCompat.STATE_STOPPED){
            customViews.setImageViewResource(R.id.tv_notify_play1,R.drawable.mv_btn_play_prs);
            customViews.setOnClickPendingIntent(R.id.tv_notify_play1,mPlayIntent);
        }else{
            customViews.setOnClickPendingIntent(R.id.tv_notify_play1,mPauseIntent);
            customViews.setImageViewResource(R.id.tv_notify_play1,R.drawable.mv_btn_pause_prs);
        }
        mBuilder.setTicker(mTitle)
                .setSmallIcon(R.mipmap.logo_notification)
                .setContent(customViews)
                .setCustomContentView(customViews)
                .setCustomBigContentView(customViews)
                .setContentIntent(contentIntent);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            mBuilder.setCustomContentView(customViews)
                    .setCustomBigContentView(bigViews);
        }
        Notification mNotification = mBuilder.build();
        mNotification.flags |= Notification.FLAG_NO_CLEAR;
//        boolean isVoice = true, isVibrate = true;
//        if (isVoice) {
//            mNotification.defaults |= Notification.DEFAULT_SOUND;
//        }
//        if (isVibrate) {
//            mNotification.defaults |= Notification.DEFAULT_VIBRATE;
//        }
//        mNotification.defaults|=Notification.DEFAULT_LIGHTS;
//        mNotification.flags = Notification.FLAG_NO_CLEAR;// 永久在通知栏里?
        // 使用自定义下拉视图时，不需要再调用setLatestEventInfo()方法，但是必须定义contentIntent
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        return mNotification;
    }
    @Override
    public void call(MusicChangeEvent changeEvent) {

    }

    public void startNotification() {
        mMetadata = mController.getMetadata();
        mPlaybackState = mService.getCurrentState();
        if(mMetadata!=null && !ACTION_CLEAR.equals(mAction)){
            mNotification = createNotification();
//            if (mNotification != null) {
//                mService.startForeground(NOTIFICATION_ID, mNotification);
//            }
        }else{
            mAction = "";
        }
    }


    public void release(){
        if(mIntentFilter!=null){
            mService.unregisterReceiver(this);
        }
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        mAction = intent.getAction();
        switch (mAction) {
            case ACTION_PAUSE:
                mService.pause();
                break;
            case ACTION_PLAY:
                mService.play();
                break;
            case ACTION_NEXT:
                mService.skipToNext();
                break;
            case ACTION_PREV:
                mService.skipToPrevious();
                break;
            case ACTION_STOP:
                mService.stop();
                break;
            case ACTION_CLEAR:
                mService.stop();
                if(mNotification!=null){
                    mNotification.flags = Notification.FLAG_AUTO_CANCEL;
                }
                mNotificationManager.cancel(NOTIFICATION_ID);
                break;
        }
    }
}
