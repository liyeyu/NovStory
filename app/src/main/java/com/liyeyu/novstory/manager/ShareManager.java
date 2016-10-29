package com.liyeyu.novstory.manager;

import android.content.Intent;
import android.support.v4.media.MediaMetadataCompat;

import com.liyeyu.novstory.manager.base.BaseManager;

/**
 * Created by Liyeyu on 2016/7/21.
 */
public class ShareManager extends BaseManager {
    public static void share(MediaMetadataCompat mMetadata){
        if(mMetadata==null){
            return;
        }
        String mTitle = mMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
        String mSinger = mMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Intent.EXTRA_TEXT, ""+mTitle+" - "+mSinger+"\n  -「From NovStory」");
        intent.setType("text/plain");
        mApp.startActivity(intent);
    }

}
