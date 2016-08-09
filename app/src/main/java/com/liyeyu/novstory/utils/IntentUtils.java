package com.liyeyu.novstory.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.liyeyu.novstory.Constants;
import com.liyeyu.novstory.act.AboutActivity;
import com.liyeyu.novstory.act.MusicListActivity;
import com.liyeyu.novstory.act.MusicPlayActivity;
import com.liyeyu.novstory.act.SettingsActivity;
import com.liyeyu.novstory.entry.Audio;

/**
 * Created by Liyeyu on 2016/7/19.
 */
public class IntentUtils {

    public static void startPlayActivity(Context Context, Audio audio){
        Intent intent = new Intent(Context, MusicPlayActivity.class);
        intent.putExtra(Constants.TAG,audio);
        Context.startActivity(intent);
    }
    public static void startPlayActivity(Activity Context, Audio audio){
        Intent intent = new Intent(Context, MusicPlayActivity.class);
        intent.putExtra(Constants.TAG,audio);
        Context.startActivityForResult(intent,Constants.LOVE_STATE_CHANGE);
    }

    public static void startPlayListActivity(Context Context){
        Intent intent = new Intent(Context, MusicListActivity.class);
        Context.startActivity(intent);
    }
    public static void startSettingActivity(Context Context){
        Intent intent = new Intent(Context, SettingsActivity.class);
        Context.startActivity(intent);
    }
    public static void startAboutActivity(Context Context){
        Intent intent = new Intent(Context, AboutActivity.class);
        Context.startActivity(intent);
    }
}
