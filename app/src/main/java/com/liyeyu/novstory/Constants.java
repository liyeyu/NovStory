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
    public static final int LIST_ITEM_NUM = 20;

    public static final String LRC_URL = "http://geci.me/api/lyric/";
    public static final String LRC_URL_KUGOU = "http://apis.baidu.com/geekery/music/";
    public static final String LRC_URL_BAIDU = "http://tingapi.ting.baidu.com/v1/restserver/";
    public static final String SONG_URL_BAIDU_METHOD = "baidu.ting.search.catalogSug";
    public static final String LRC_URL_BAIDU_METHOD = "baidu.ting.song.lry";
    public static final String BASE_URL = LRC_URL_BAIDU;
    public static final String APIKEY_BAIDU = "7fb1aea1948021894cc76bf3c160f6e6";

    /**
     * 文件存储目录
     */
    public static final  String LOCAL_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +"novplay";
}
