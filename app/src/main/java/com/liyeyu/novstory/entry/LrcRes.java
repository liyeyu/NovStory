package com.liyeyu.novstory.entry;

import java.util.List;

/**
 * Created by Liyeyu on 2016/8/22.
 */
public class LrcRes {

    /**
     * count : 15
     * code : 0
     * result : [{"aid":1563419,"artist_id":9208,"song":"海阔天空","lrc":"http://s.gecimi.com/lrc/166/16685/1668536.lrc","sid":1668536},{"aid":1567586,"artist_id":9208,"song":"海阔天空","lrc":"http://s.gecimi.com/lrc/167/16739/1673997.lrc","sid":1673997},{"aid":1571906,"artist_id":9208,"song":"海阔天空","lrc":"http://s.gecimi.com/lrc/167/16796/1679605.lrc","sid":1679605},{"aid":1573814,"artist_id":9208,"song":"海阔天空","lrc":"http://s.gecimi.com/lrc/168/16819/1681961.lrc","sid":1681961},{"aid":1656038,"artist_id":9208,"song":"海阔天空","lrc":"http://s.gecimi.com/lrc/179/17907/1790768.lrc","sid":1790768},{"aid":1718741,"artist_id":9208,"song":"海阔天空","lrc":"http://s.gecimi.com/lrc/187/18757/1875769.lrc","sid":1875769},{"aid":2003267,"artist_id":9208,"song":"海阔天空","lrc":"http://s.gecimi.com/lrc/226/22642/2264296.lrc","sid":2264296},{"aid":2020610,"artist_id":9208,"song":"海阔天空","lrc":"http://s.gecimi.com/lrc/228/22889/2288967.lrc","sid":2288967},{"aid":2051678,"artist_id":9208,"song":"海阔天空","lrc":"http://s.gecimi.com/lrc/233/23323/2332322.lrc","sid":2332322},{"aid":2412704,"artist_id":9208,"song":"海阔天空","lrc":"http://s.gecimi.com/lrc/283/28376/2837689.lrc","sid":2837689},{"aid":2607041,"artist_id":9208,"song":"海阔天空","lrc":"http://s.gecimi.com/lrc/311/31116/3111659.lrc","sid":3111659},{"aid":2647055,"artist_id":9208,"song":"海阔天空","lrc":"http://s.gecimi.com/lrc/316/31663/3166350.lrc","sid":3166350},{"aid":2657468,"artist_id":9208,"song":"海阔天空","lrc":"http://s.gecimi.com/lrc/318/31803/3180339.lrc","sid":3180339},{"aid":3093833,"artist_id":9208,"song":"海阔天空","lrc":"http://s.gecimi.com/lrc/377/37740/3774083.lrc","sid":3774083},{"aid":3161846,"artist_id":9208,"song":"海阔天空","lrc":"http://s.gecimi.com/lrc/386/38612/3861244.lrc","sid":3861244}]
     */

    private int count;
    private int code;
    /**
     * aid : 1563419
     * artist_id : 9208
     * song : 海阔天空
     * lrc : http://s.gecimi.com/lrc/166/16685/1668536.lrc
     * sid : 1668536
     */

    private List<ResultBean> result;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public List<ResultBean> getResult() {
        return result;
    }

    public void setResult(List<ResultBean> result) {
        this.result = result;
    }

    public static class ResultBean {
        private int aid;
        private int artist_id;
        private String song;
        private String lrc;
        private int sid;

        public int getAid() {
            return aid;
        }

        public void setAid(int aid) {
            this.aid = aid;
        }

        public int getArtist_id() {
            return artist_id;
        }

        public void setArtist_id(int artist_id) {
            this.artist_id = artist_id;
        }

        public String getSong() {
            return song;
        }

        public void setSong(String song) {
            this.song = song;
        }

        public String getLrc() {
            return lrc;
        }

        public void setLrc(String lrc) {
            this.lrc = lrc;
        }

        public int getSid() {
            return sid;
        }

        public void setSid(int sid) {
            this.sid = sid;
        }
    }
}
