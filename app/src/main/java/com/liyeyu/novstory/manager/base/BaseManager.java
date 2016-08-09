package com.liyeyu.novstory.manager.base;

import com.liyeyu.novstory.NovStoryApp;

/**
 * Created by Liyeyu on 2016/7/21.
 */
public class BaseManager {
    public static NovStoryApp mApp;
    public static void init(NovStoryApp app){
        mApp = app;
    }
}
