package com.liyeyu.novstory.manager;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.liyeyu.novstory.Constants;
import com.liyeyu.novstory.R;
import com.liyeyu.novstory.act.MusicPlayActivity;
import com.liyeyu.novstory.play.PlayControlService;
import com.liyeyu.novstory.utils.ImageLoader;
import com.liyeyu.novstory.utils.ResourceHelper;

/**
 * Created by Liyeyu on 2016/7/21.
 */
public class PlayNotificationManager1 extends BroadcastReceiver {
    private static final String TAG = PlayNotificationManager1.class.getSimpleName();
    public static final int NOTIFICATION_ID = 1129;
    private static final int REQUEST_CODE = 100;
    public static final String ACTION_PAUSE = Constants.PACKAGE_NAME +".pause";
    public static final String ACTION_PLAY = Constants.PACKAGE_NAME +".play";
    public static final String ACTION_PREV = Constants.PACKAGE_NAME +".prev";
    public static final String ACTION_NEXT = Constants.PACKAGE_NAME +".next";
    public static final String ACTION_STOP_CASTING = Constants.PACKAGE_NAME +".stop_cast";
    private final int mNotificationColor;
    private final NotificationManagerCompat mNotificationManager;
    private final PlayControlService mService;
    private final PendingIntent mPauseIntent;
    private final PendingIntent mPlayIntent;
    private final PendingIntent mPreviousIntent;
    private final PendingIntent mNextIntent;
    private final PendingIntent mStopCastIntent;
    private final MediaControllerCompat mController;
    private final MediaControllerCompat.TransportControls mTransportControls;
    private boolean mStarted = false;
    private MediaMetadataCompat mMetadata;
    private PlaybackStateCompat mPlaybackState;
    private final String mAppName;


    public PlayNotificationManager1(PlayControlService service) throws RemoteException {
        mService = service;
        mAppName = mService.getString(R.string.app_name);
        mNotificationColor = ResourceHelper.getThemeColor(mService, R.attr.colorPrimary,
                service.getResources().getColor(R.color.gray_f5));
        mNotificationManager = NotificationManagerCompat.from(service);
        mController = mService.getMediaController();
        mTransportControls = mService.getTransportControls();
        mPlaybackState = mController.getPlaybackState();

        String pkg = mService.getPackageName();
        mPauseIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PAUSE).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPlayIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PLAY).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPreviousIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PREV).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mNextIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_NEXT).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mStopCastIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_STOP_CASTING).setPackage(pkg),
                PendingIntent.FLAG_CANCEL_CURRENT);

        // Cancel all notifications to handle the case where the Service was killed and
        // restarted by the system.
        mNotificationManager.cancelAll();
    }
    // 通知栏显示当前播放信息，利用通知和 PendingIntent来启动对应的activity
    public Notification showNotification(Context context, Intent intent) {

        if (intent == null) {
            return null;
        }

        android.support.v7.app.NotificationCompat.Builder notificationBuilder = new android.support.v7.app.NotificationCompat.Builder(mService);
        int playPauseButtonPosition = 0;

        // If skip to previous action is enabled
//        if ((mPlaybackState.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0) {
//            notificationBuilder.addAction(R.drawable.ic_skip_previous_white_24dp,
//                    mService.getString(R.string.label_previous), mPreviousIntent);
//            playPauseButtonPosition = 1;
//        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context);
        //兼容 6.0通知栏样式
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setAutoCancel(false)
                .setContentIntent(contentIntent);
        RemoteViews bigViews = new RemoteViews(context.getPackageName(), R.layout.notification_play);
        bigViews.setImageViewResource(R.id.iv_music_icon, ImageLoader.DEF_BG);
        bigViews.setTextViewText(R.id.tv_notify_title,mAppName);
        bigViews.setTextViewText(R.id.tv_notify_singer, mAppName);
        builder.setCustomBigContentView(bigViews);

//        builder.setStyle(new android.support.v7.app.NotificationCompat.MediaStyle()
//                        .setShowActionsInCompactView(
//                                new int[]{playPauseButtonPosition})  // show only play/pause in compact view
//                        .setMediaSession(NovPlayController.getController().getToken()))
//                .setColor(mNotificationColor)
//                .setSmallIcon(R.mipmap.ic_notification)
//                .setVisibility(android.support.v7.app.NotificationCompat.VISIBILITY_PUBLIC)
//                .setUsesChronometer(true)
//                .setContentIntent(contentIntent)
//                .setContentTitle(mAppName)
//                .setContentText(mAppName)
//                .setLargeIcon(bitmap);

        RemoteViews customViews = new RemoteViews(context.getPackageName(), R.layout.notification_play_custom);
        builder.setCustomContentView(customViews);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            builder.setSmallIcon(R.mipmap.ic_notification);
        }
        Notification mNotification = builder.build();
        boolean isVoice = true, isVibrate = true;
        if (isVoice) {
            mNotification.defaults |= Notification.DEFAULT_SOUND;
        }
        if (isVibrate) {
            mNotification.defaults |= Notification.DEFAULT_VIBRATE;
        }
        mNotification.defaults|=Notification.DEFAULT_LIGHTS;
        mNotification.flags = Notification.FLAG_NO_CLEAR;// 永久在通知栏里?
//        mNotification.flags = Notification.FLAG_AUTO_CANCEL;
        // 使用自定义下拉视图时，不需要再调用setLatestEventInfo()方法，但是必须定义contentIntent
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        return mNotification;
    }

    public void startNotification() {
        if (!mStarted) {
            mMetadata = mController.getMetadata();
            // The notification must be updated after setting started to true
            Notification notification = showNotification(mService,new Intent(mService, MusicPlayActivity.class));
            if (notification != null) {
                mController.registerCallback(mCb);
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_NEXT);
                filter.addAction(ACTION_PAUSE);
                filter.addAction(ACTION_PLAY);
                filter.addAction(ACTION_PREV);
                filter.addAction(ACTION_STOP_CASTING);
                mService.registerReceiver(this, filter);

                mService.startForeground(NOTIFICATION_ID, notification);
                mStarted = true;
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d(TAG, "Received intent with action " + action);
        switch (action) {
            case ACTION_PAUSE:
                mTransportControls.pause();
                break;
            case ACTION_PLAY:
                mTransportControls.play();
                break;
            case ACTION_NEXT:
                mTransportControls.skipToNext();
                break;
            case ACTION_PREV:
                mTransportControls.skipToPrevious();
                break;
            case ACTION_STOP_CASTING:
//                Intent i = new Intent(context, MusicService.class);
//                i.setAction(MusicService.ACTION_CMD);
//                i.putExtra(MusicService.CMD_NAME, MusicService.CMD_STOP_CASTING);
//                mService.startService(i);
                break;
            default:
                Log.w(TAG, "Unknown intent ignored. Action="+action);
        }
    }


    private final MediaControllerCompat.Callback mCb = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
            mPlaybackState = state;
            Log.d(TAG, "Received new playback state"+state);
            if (state.getState() == PlaybackStateCompat.STATE_STOPPED ||
                    state.getState() == PlaybackStateCompat.STATE_NONE) {
                stopNotification();
            } else {
                Notification notification = showNotification(mService,new Intent(mService, MusicPlayActivity.class));
                if (notification != null) {
                    mNotificationManager.notify(NOTIFICATION_ID, notification);
                }
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            mMetadata = metadata;
            Log.d(TAG, "Received new metadata "+metadata.getDescription().getTitle());
            Notification notification = showNotification(mService,new Intent(mService, MusicPlayActivity.class));
            if (notification != null) {
                mNotificationManager.notify(NOTIFICATION_ID, notification);
            }
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
            Log.d(TAG, "Session was destroyed, resetting to the new session token");
//            try {
//                updateSessionToken();
//            } catch (RemoteException e) {
//                Log.e(TAG,  "could not connect media controller");
//            }
        }
    };

    public void stopNotification() {
        if (mStarted) {
            mStarted = false;
            mController.unregisterCallback(mCb);
            try {
                mNotificationManager.cancel(NOTIFICATION_ID);
                mService.unregisterReceiver(this);
            } catch (IllegalArgumentException ex) {
                // ignore if the receiver is not registered.
            }
            mService.stopForeground(true);
        }
    }

    private void addPlayPauseAction(android.support.v7.app.NotificationCompat.Builder builder) {
        Log.d(TAG, "updatePlayPauseAction");
        String label;
        int icon;
        PendingIntent intent;
        if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            label = mService.getString(R.string.label_pause);
            icon = R.drawable.uamp_ic_pause_white_24dp;
            intent = mPauseIntent;
        } else {
            label = mService.getString(R.string.label_play);
            icon = R.drawable.uamp_ic_play_arrow_white_24dp;
            intent = mPlayIntent;
        }
        builder.addAction(new android.support.v7.app.NotificationCompat.Action(icon, label, intent));
    }
}
