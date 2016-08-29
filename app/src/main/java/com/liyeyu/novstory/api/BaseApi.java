package com.liyeyu.novstory.api;

import com.liyeyu.novstory.entry.LrcBDRes;
import com.liyeyu.novstory.entry.LrcKuGouRes;
import com.liyeyu.novstory.entry.LrcKuGouSongRes;
import com.liyeyu.novstory.entry.LrcRes;
import com.liyeyu.novstory.entry.SongBDRes;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/**
 * Created by Liyeyu on 2016/8/22.
 */
public interface BaseApi{
    @GET
    Call<LrcRes> get(@Url String url);
    @GET("ting")
    Call<SongBDRes> getBDSong(@Query("method") String method, @Query("query") String query);
    @GET("ting")
    Call<LrcBDRes> getBDLrc(@Query("method") String method, @Query("songid") String songid);
    @GET("query")
    Call<LrcKuGouSongRes> getKGSong(@Header("apikey") String apiKey, @QueryMap() Map<String,String> params);
    @GET("krc")
    Call<LrcKuGouRes> getKGLrc(@Header("apikey") String apiKey, @QueryMap() Map<String,String> params);
}
