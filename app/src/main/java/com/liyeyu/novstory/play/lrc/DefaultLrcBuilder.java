package com.liyeyu.novstory.play.lrc;

import android.text.TextUtils;
import android.util.Log;

import com.liyeyu.novstory.Constants;
import com.liyeyu.novstory.api.BaseApi;
import com.liyeyu.novstory.entry.LrcBDRes;
import com.liyeyu.novstory.entry.LrcKuGouRes;
import com.liyeyu.novstory.entry.LrcKuGouSongRes;
import com.liyeyu.novstory.entry.SongBDRes;
import com.liyeyu.novstory.utils.CommUtils;
import com.liyeyu.rxhttp.RetrofitHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;

/**
 * 解析歌词，得到LrcRow的集合
 */
public class DefaultLrcBuilder implements ILrcBuilder {
    static final String TAG = "DefaultLrcBuilder";
    private String curLrcPath;

    public List<LrcRow> getLrcRows(String rawLrc) {
        if(rawLrc == null || rawLrc.length() == 0){
            Log.e(TAG,"getLrcRows rawLrc null or empty");
            return null;
        }
        StringReader reader = new StringReader(rawLrc);
        BufferedReader br = new BufferedReader(reader);
        String line ;
        List<LrcRow> rows = new ArrayList<>();
        try{
            //循环地读取歌词的每一行
            do{
                line = br.readLine();
                /**
                 一行歌词只有一个时间的  例如：徐佳莹   《我好想你》
                 [01:15.33]我好想你 好想你

                 一行歌词有多个时间的  例如：草蜢 《失恋战线联盟》
                 [02:34.14][01:07.00]当你我不小心又想起她
                 [02:45.69][02:42.20][02:37.69][01:10.60]就在记忆里画一个叉
                 **/
                Log.d(TAG,"lrc raw line: " + line);
                if(line != null && line.length() > 0){
                    //解析每一行歌词 得到每行歌词的集合，因为有些歌词重复有多个时间，就可以解析出多个歌词行来
                    List<LrcRow> lrcRows = LrcRow.createRows(line);
                    if(lrcRows != null && lrcRows.size() > 0){
                        for(LrcRow row : lrcRows){
                            rows.add(row);
                        }
                    }
                }
            }while(line != null);

            if( rows.size() > 0 ){
                // 根据歌词行的时间排序
                Collections.sort(rows);
                if(rows!=null&&rows.size()>0){
                    for(LrcRow lrcRow:rows){
//                        Log.d(TAG, "lrcRow:" + lrcRow.toString());
                    }
                }
            }
        }catch(Exception e){
            Log.e(TAG,"parse exceptioned:" + e.getMessage());
            return null;
        }finally{
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            reader.close();
        }
        return rows;
    }

    public String getFromFile(String musicFileName){
        try {
            String endWith = ".mp3";
            if(TextUtils.isEmpty(musicFileName)){
                return "";
            }
            if(musicFileName.endsWith(".mp3")){
                endWith = ".mp3";
            }else if(musicFileName.endsWith(".wav")){
                endWith = ".wav";
            }else if( musicFileName.endsWith(".wma")){
                endWith = ".wma";
            }
            File file = new File(musicFileName.replace(endWith, ".lrc"));
            curLrcPath = file.getPath();
            if(!file.exists()){
                file = new File(musicFileName.replace(endWith, ".txt"));
                if(!file.exists()){
                    return "";
                }
            }

            InputStreamReader inputReader = new InputStreamReader(new FileInputStream(file));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line;
            String result="";
            while((line = bufReader.readLine()) != null){
                if(line.trim().equals(""))
                    continue;
                result += line + "\r\n";
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void getLrcFromUrl(final String path,final String songName, final String artist,final OnLrcLoadListener listener){
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

    public void getSongFromKG(final String path,final String songName, final String artist,final OnLrcLoadListener listener){
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


    public void getLrcFromKG(final String path,final LrcKuGouSongRes.DataBean.LrcKGInner inner,final OnLrcLoadListener listener){
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
                    CommUtils.writeFile(path,res.getData().getContent());
                    if(listener!=null){
                        listener.onSuccess();
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


    public void getSongFromBD(final String path,final String songName, final String artist,final OnLrcLoadListener listener){
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

    private void getLrcFromBD(final String songId,final OnLrcLoadListener listener){
        RetrofitHelper.request(BaseApi.class, new RetrofitHelper.HttpCallBack<LrcBDRes, BaseApi>() {
            @Override
            public Call<LrcBDRes> request(BaseApi request) {
                return request.getBDLrc(Constants.LRC_URL_BAIDU_METHOD,songId);
            }

            @Override
            public void onCompleted(LrcBDRes lrcRes) {
                if(lrcRes!=null && !TextUtils.isEmpty(lrcRes.getLrcContent())){
                    CommUtils.writeFile(curLrcPath,lrcRes.getLrcContent());
                    if(listener!=null){
                        listener.onSuccess();
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

    public String getCurLrcPath() {
        return curLrcPath;
    }

    public interface OnLrcLoadListener{
       void  onSuccess();
        void  onError();
    }
}
