package com.liyeyu.novstory;

import android.os.Environment;

import java.io.File;

/**
 * Created by Liyeyu on 2016/7/22.
 */
public class Constants {
    public static final String PACKAGE_NAME = "com.liyeyu.novplay";
    public static final String TAG = "tag";
    public static final String LOVE_SONG= "love_song";
    public static final String FIRST_LAUNCH= "first_launch";
    public static final String PLAY_MODE= "play_mode";
    public static final String MEDIA_ID = "media_id";
    public static final String LAST_PROGRESS = "last_progress";
    public static final String SETTING_DURATION = "setting_duration";
    public static final int LOVE_STATE_CHANGE = 100;
    /**
     * 文件存储目录
     */
    public static final  String LOCAL_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +"novplay";
}
