package com.liyeyu.novstory.api;

import android.text.TextUtils;

import com.liyeyu.lrcview.lrc.LrcBuilder;
import com.liyeyu.lrcview.lrc.LrcInfo;
import com.liyeyu.novstory.Constants;
import com.liyeyu.novstory.entry.LrcBDRes;
import com.liyeyu.novstory.entry.LrcKuGouRes;
import com.liyeyu.novstory.entry.LrcKuGouSongRes;
import com.liyeyu.novstory.entry.SongBDRes;
import com.liyeyu.novstory.utils.CommUtils;
import com.liyeyu.rxhttp.RetrofitHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;

/**
 * Created by Liyeyu on 2016/10/22.
 */

public class LrcHelper {
    private static LrcBuilder lrcBuilder;
    static {
        lrcBuilder = new LrcBuilder();
    }

    public static InputStream getStream(String musicFileName){
        return lrcBuilder.getInputStreamFromMusic(musicFileName);
    }

    public static void getFromFile(LrcBuilder.OnLrcLoadListener loadListener){
        lrcBuilder.setLoadListener(loadListener);
        lrcBuilder.load();
    }
    public static LrcInfo getFromFile(String musicFileName){
        LrcInfo lrcInfo = lrcBuilder.parseLrcFromMusic(musicFileName);
        return lrcInfo;
    }

    public static void getLrcFromUrl(final String path,final String songName, final String artist,final OnLrcLoadListener listener){
        RetrofitHelper.isDebug = false;
        getSongFromKG(path,songName, artist, new OnLrcLoadListener() {
            @Override
            public void onSuccess() {
                if(listener!=null){
                    listener.onSuccess();
                }
            }

            @Override
            public void onError() {
                getSongFromBD(path,songName,artist,listener);
            }
        });
    }

    public static void getSongFromKG(final String path,final String songName, final String artist,final OnLrcLoadListener listener){
        RetrofitHelper.setBaseUrl(Constants.LRC_URL_KUGOU);
        RetrofitHelper.request(BaseApi.class, new RetrofitHelper.HttpCallBack<LrcKuGouSongRes, BaseApi>() {

            @Override
            public Call<LrcKuGouSongRes> request(BaseApi request) {
                Map<String,String> map = new HashMap<>();
                map.put("s",songName);
                map.put("size","15");
                map.put("page","1");
                return request.getKGSong(Constants.APIKEY_BAIDU,map);
            }

            @Override
            public void onCompleted(LrcKuGouSongRes res) {
                LrcKuGouSongRes.DataBean data = res.getData();
                if(data!=null && data.getData()!=null && !data.getData().isEmpty()){
                    List<LrcKuGouSongRes.DataBean.LrcKGInner> list = data.getData();
                    LrcKuGouSongRes.DataBean.LrcKGInner songBean = list.get(0);
                    for (LrcKuGouSongRes.DataBean.LrcKGInner item:list) {
                        if(artist.contains(item.getSingername())){
                            songBean = item;
                        }
                    }
                    songBean.setFilename(songName);
                    getLrcFromKG(path,songBean,listener);
                }else{
                    if(listener!=null){
                        listener.onError();
                    }
                }
            }

            @Override
            public void onError(String message) {
                if(listener!=null){
                    listener.onError();
                }
            }
        });
    }


    public static void getLrcFromKG(final String path,final LrcKuGouSongRes.DataBean.LrcKGInner inner,final OnLrcLoadListener listener){
        RetrofitHelper.request(BaseApi.class, new RetrofitHelper.HttpCallBack<LrcKuGouRes, BaseApi>() {

            @Override
            public Call<LrcKuGouRes> request(BaseApi request) {
                Map<String,String> map = new HashMap<>();
                map.put("name",inner.getFilename());
                map.put("hash",inner.getHash());
                map.put("time",inner.getDuration()+"");
                return request.getKGLrc(Constants.APIKEY_BAIDU,map);
            }

            @Override
            public void onCompleted(LrcKuGouRes res) {
                if(res!=null && res.getData()!=null && !TextUtils.isEmpty(res.getData().getContent())){
                    try {
                        CommUtils.writeFile(path,res.getData().getContent());
                        if(listener!=null){
                            listener.onSuccess();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        if(listener!=null){
                            listener.onError();
                        }
                    }

                }else{
                    if(listener!=null){
                        listener.onError();
                    }
                }
            }

            @Override
            public void onError(String message) {
                if(listener!=null){
                    listener.onError();
                }
            }
        });
    }


    public static void getSongFromBD(final String path,final String songName, final String artist,final OnLrcLoadListener listener){
        RetrofitHelper.setBaseUrl(Constants.LRC_URL_BAIDU);
        RetrofitHelper.request(BaseApi.class, new RetrofitHelper.HttpCallBack<SongBDRes, BaseApi>() {
            @Override
            public Call<SongBDRes> request(BaseApi request) {
                String url = songName;
                return request.getBDSong(Constants.SONG_URL_BAIDU_METHOD,url);
            }

            @Override
            public void onCompleted(SongBDRes lrcRes) {
                if(lrcRes!=null){
                    List<SongBDRes.SongBean> song = lrcRes.getSong();
                    if(song!=null && !song.isEmpty()){
                        SongBDRes.SongBean songBean = song.get(0);
                        for (SongBDRes.SongBean item:song) {
                            if(artist.contains(item.getArtistname())){
                                songBean = item;
                            }
                        }
                        getLrcFromBD(songBean.getSongid(),listener);
                    }else{
                        if(listener!=null){
                            listener.onError();
                        }
                    }
                }else{
                    if(listener!=null){
                        listener.onError();
                    }
                }
            }

            @Override
            public void onError(String message) {
                if(listener!=null){
                    listener.onError();
                }
            }
        });
    }

    private static void getLrcFromBD(final String songId,final OnLrcLoadListener listener){
        RetrofitHelper.request(BaseApi.class, new RetrofitHelper.HttpCallBack<LrcBDRes, BaseApi>() {
            @Override
            public Call<LrcBDRes> request(BaseApi request) {
                return request.getBDLrc(Constants.LRC_URL_BAIDU_METHOD,songId);
            }

            @Override
            public void onCompleted(LrcBDRes lrcRes) {
                if(lrcRes!=null && !TextUtils.isEmpty(lrcRes.getLrcContent())){
                    try {
                        CommUtils.writeFile(lrcBuilder.curLrcPath,lrcRes.getLrcContent());
                        if(listener!=null){
                            listener.onSuccess();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        if(listener!=null){
                            listener.onError();
                        }
                    }
                }else{
                    if(listener!=null){
                        listener.onError();
                    }
                }
            }

            @Override
            public void onError(String message) {
                if(listener!=null){
                    listener.onError();
                }
            }
        });
    }

    public static String getCurLrcPath() {
        return lrcBuilder.curLrcPath;
    }

    public interface OnLrcLoadListener{
        void  onSuccess();
        void  onError();
    }
}
