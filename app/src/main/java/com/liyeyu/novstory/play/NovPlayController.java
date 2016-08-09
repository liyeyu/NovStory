package com.liyeyu.novstory.play;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;

import com.liyeyu.novstory.Constants;
import com.liyeyu.novstory.entry.Audio;
import com.liyeyu.novstory.manager.AppConfig;
import com.liyeyu.novstory.manager.base.BaseManager;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Liyeyu on 2016/7/22.
 */
public class NovPlayController extends BaseManager{
    public static NovPlayController controller;
    public PlayControlService mPlayServer;
    private NovPlayController() {

    }
    public static NovPlayController get() {
        if (controller == null) {
            synchronized (NovPlayController.class) {
                if (controller == null) {
                    controller = new NovPlayController();
                }
            }
        }
        return controller;
    }

    /**
     * 初始化构造器
     */
    public  void initControl(PlayControlService context) {
        mPlayServer = context;
    }
    /**
     * 播放
     * @param audio
     * @return  是否在播放当前id的音频
     */
    public void play(Audio audio) {
        mPlayServer.play(audio);
    }

    public void play(MediaMetadataCompat curMetadata) {
        mPlayServer.play(curMetadata);
    }

    public void pause(){
        mPlayServer.pause();
    }
    public void play(){
        mPlayServer.play();
    }
    public void stop(){
        mPlayServer.stop();
    }

    public void seekTo(long pos){
        mPlayServer.seekTo(pos);
    }
    public void onSkipToNext(){
        mPlayServer.skipToNext();
    }
    public void onSkipToPrevious(){
        mPlayServer.skipToPrevious();
    }

    public int getCurrentState(){
        return mPlayServer.getCurrentState();
    }
    public MediaMetadataCompat getCurrentMetaDate(){
        return mPlayServer.getCurrentMetaDate();
    }

    public MediaSessionCompat getSession() {
        return mPlayServer.getSession();
    }

    public MediaControllerCompat getMediaController() {
        return mPlayServer.getMediaController();
    }

    public int getCurrentStreamPosition() {
        return mPlayServer.getCurrentStreamPosition();
    }

    public int getSkipQueuePosition(int amount) {
        return mPlayServer.getSkipQueuePosition(amount);
    }
    public MediaMetadataCompat getMediaData(int index) {
        return mPlayServer.getMediaData(index);
    }

    public void updateQueue(int fromPosition, int toPosition) {
        mPlayServer.updateQueue(fromPosition,toPosition);
    }
    public void remove(int position) {
        mPlayServer.remove(position);
    }
    public long getCurrentMediaId() {
        return mPlayServer.getCurrentMediaId();
    }

    public void setConfigMetaDate(){
        mPlayServer.setConfigMetaDate();
    }

    public int nextMode() {
        return MediaQueueManager.nextMode();
    }

    public void loveSongs(long id,boolean check) {
        String s = AppConfig.get(Constants.LOVE_SONG, "");
        if(check){
            s += "#"+id;
        }else{
            if(!TextUtils.isEmpty(s)){
                List<String> list = Arrays.asList(s.split("#"));
                if(list.contains(String.valueOf(id))){
                    s = "";
                    for (String item:list) {
                        if(!item.equals(String.valueOf(id))){
                            s += "#"+item;
                        }
                    }
                }
            }
        }
        AppConfig.save(Constants.LOVE_SONG,s);
    }
    public boolean isLove(long id) {
        String s = AppConfig.get(Constants.LOVE_SONG, "");
        if(!TextUtils.isEmpty(s)){
            List<String> list = Arrays.asList(s.split("#"));
            return list.contains(String.valueOf(id));
        }
        return false;
    }
}
