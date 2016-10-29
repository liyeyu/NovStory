package com.liyeyu.novstory.play;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;

import com.liyeyu.novstory.Constants;
import com.liyeyu.novstory.entry.Audio;
import com.liyeyu.novstory.manager.AppConfig;
import com.liyeyu.novstory.manager.base.BaseManager;
import com.liyeyu.novstory.utils.MediaUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import liyeyu.support.utils.utils.LogUtil;

/**
 * Created by Liyeyu on 2016/7/22.
 */
public class MediaQueueManager extends BaseManager{
    public static final int PLAY_MODE_ORDER = 0;
    public static final int PLAY_MODE_RANDOM = 1;
    public static final int PLAY_MODE_SINGLE = 2;
    public static final int PLAY_MODE_SINGLE_LOOP = 3;
    public static final int[] MODES = new int[]{
            PLAY_MODE_ORDER,
            PLAY_MODE_RANDOM,
            PLAY_MODE_SINGLE,
            PLAY_MODE_SINGLE_LOOP};
    public static final String ALL = "all";
    public static final String LOVE = "love";
    public static String CURRENT_TAG = ALL;
    public int mCurrentIndex = 0;
    private static MediaQueueManager queueManager;
    public static List<MediaSessionCompat.QueueItem> LoveQueueItems = new ArrayList<>();
    public static List<MediaSessionCompat.QueueItem> AllQueueItems = new ArrayList<>();
    private volatile static List<MediaSessionCompat.QueueItem> CurrentPlayQueueItems = new ArrayList<>();
    public static Map<String,List<MediaSessionCompat.QueueItem>> QueueTitles = new HashMap<>();
    public static Map<Long,MediaMetadataCompat> metaDataIds = new HashMap<>();
    public List<Audio> mAudioList = new ArrayList<>();
    public List<Audio> mLoveList = new ArrayList<>();
    public static List<String> mLoveSongs;

    private MediaQueueManager() {
        QueueTitles.put(ALL,AllQueueItems);
        QueueTitles.put(LOVE,LoveQueueItems);
    }

    public static MediaQueueManager get(){
        if(queueManager==null){
            synchronized (MediaQueueManager.class){
                if(queueManager==null){
                    queueManager = new MediaQueueManager();
                }
            }
        }
        return queueManager;
    }

    public List<MediaSessionCompat.QueueItem> getCurrentPlayQueueItems() {
        return CurrentPlayQueueItems;
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    public static int nextMode() {
        int mode = AppConfig.get(Constants.PLAY_MODE, MediaQueueManager.PLAY_MODE_ORDER);
        mode = ++mode % MODES.length;
        AppConfig.save(Constants.PLAY_MODE,mode);
        return mode;
    }

    /**
     * 查询媒体信息
     */
    public synchronized List<Audio> queryMediaList(Context context,String tag){
        CURRENT_TAG = tag;
        List<Audio> allAudioList = MediaUtils.getAllAudioList(context);
        if(allAudioList!=null){
            mAudioList.clear();
            mAudioList.addAll(allAudioList);
        }
        if(LOVE.equals(tag)){
            LoveQueueItems.clear();
            mLoveList.clear();
            String s = AppConfig.get(Constants.LOVE_SONG, "");
            if(!TextUtils.isEmpty(s)){
                mLoveSongs = Arrays.asList(s.split("#"));
            }
            if(mLoveSongs!=null){
                for (Audio audio:mAudioList) {
                    if(mLoveSongs.contains(String.valueOf(audio.getId()))){
                        mLoveList.add(audio);
                    }
                }
            }
            mAudioList.clear();
            mAudioList.addAll(mLoveList);
            updateQueue(mLoveList,LoveQueueItems);
        }else{
            AllQueueItems.clear();
            updateQueue(mAudioList,AllQueueItems);
        }
        if( NovPlayController.get().getSession()!=null){
            NovPlayController.get().getSession().setQueue(CurrentPlayQueueItems);
        }
        return mAudioList;
    }

    private synchronized void updateQueue(List<Audio> audios,List<MediaSessionCompat.QueueItem> items){
        CurrentPlayQueueItems.clear();
        for (Audio audio:audios) {
            MediaMetadataCompat compat = CreateMetaData(audio);
            MediaSessionCompat.QueueItem queueItem = new MediaSessionCompat.QueueItem(compat.getDescription(),audio.getId());
            items.add(queueItem);
            CurrentPlayQueueItems.add(queueItem);
            metaDataIds.put((long) audio.getId(),compat);
        }
        LogUtil.i("updateQueue QueueItems:" +CurrentPlayQueueItems.size());
    }

    public synchronized void updateQueue(int fromPosition, int toPosition) {
        if(!CurrentPlayQueueItems.isEmpty()
                && fromPosition<CurrentPlayQueueItems.size()
                && toPosition<CurrentPlayQueueItems.size()){

            long id = Long.parseLong( getCurrentMetaData().getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
            Collections.swap(CurrentPlayQueueItems,fromPosition,toPosition);
            Collections.swap(mAudioList,fromPosition,toPosition);
            updateCurrentPos(id);
            NovPlayController.get().getSession().setQueue(CurrentPlayQueueItems);
        }
    }

    public void remove(int position, boolean isCur) {
        if(!CurrentPlayQueueItems.isEmpty()){
            CurrentPlayQueueItems.remove(position);
            if(isCur){
                mCurrentIndex = 0;
            }
            NovPlayController.get().getSession().setQueue(CurrentPlayQueueItems);
        }
    }

    public MediaMetadataCompat getCurrentMetaData(){
        return getMetaData(mCurrentIndex);
    }
    public MediaMetadataCompat getMetaData(long id){
        if(metaDataIds.containsKey(id)){
            return metaDataIds.get(id);
        }
        return null;
    }
    public MediaMetadataCompat getMetaData(int index){
        if (!isIndexPlayable(index, CurrentPlayQueueItems)) {
            return null;
        }
        MediaSessionCompat.QueueItem queueItem = CurrentPlayQueueItems.get(index);
        MediaMetadataCompat compat = metaDataIds.containsKey(queueItem.getQueueId()) ? metaDataIds.get(queueItem.getQueueId()) : null;
        return compat;
    }
    public int updateCurrentPos(long id){
        if(CurrentPlayQueueItems!=null && !CurrentPlayQueueItems.isEmpty()){
            for (MediaSessionCompat.QueueItem item:CurrentPlayQueueItems) {
                if(item.getQueueId()==id){
                    mCurrentIndex = CurrentPlayQueueItems.indexOf(item);
                }
            }
        }
        return mCurrentIndex;
    }

    public static MediaMetadataCompat CreateMetaData(Audio audio){
        return new  MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,audio.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM,audio.getAlbum())
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER,audio.getAlbumId())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE,audio.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,audio.getId()+"")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,audio.getPath())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, audio.getDuration())
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, MediaUtils.getDefaultArtwork(audio.getPath()))
                .build();
    }

    private boolean isIndexPlayable(int index, List<MediaSessionCompat.QueueItem> queue) {
        return (queue != null && index >= 0 && index < queue.size());
    }
    public boolean skipQueuePosition(int amount) {

        if(CurrentPlayQueueItems.isEmpty()){
            return false;
        }

        int index = mCurrentIndex + amount;
        if (index < 0) {
            // skip backwards before the first song will keep you on the first song
            index = Math.abs(CurrentPlayQueueItems.size()+index)%CurrentPlayQueueItems.size();
        } else {
            // skip forwards when in last song will cycle back to start of the queue
            index %= CurrentPlayQueueItems.size();
        }
        if (!isIndexPlayable(index, CurrentPlayQueueItems)) {
            return false;
        }
        mCurrentIndex = index;
        return true;
    }
    public int getSkipQueuePosition(int amount) {
        int index = mCurrentIndex + amount;
        if (index < 0) {
            // skip backwards before the first song will keep you on the first song
            index = Math.abs(CurrentPlayQueueItems.size()+index)%CurrentPlayQueueItems.size();
        } else {
            // skip forwards when in last song will cycle back to start of the queue
            index %= CurrentPlayQueueItems.size();
        }
        if (!isIndexPlayable(index, CurrentPlayQueueItems)) {
            return -1;
        }
        return index;
    }

}
