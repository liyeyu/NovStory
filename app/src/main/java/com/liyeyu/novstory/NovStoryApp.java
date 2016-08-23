package com.liyeyu.novstory;

import android.app.Application;
import android.content.Intent;

import com.liyeyu.novstory.manager.base.BaseManager;
import com.liyeyu.novstory.play.PlayControlService;
import com.liyeyu.novstory.utils.CrashHandler;
import com.liyeyu.novstory.utils.ImageLoader;
import com.liyeyu.rxhttp.RxHttpConfig;

import liyeyu.support.utils.SupportUtilsConfig;

/**
 * Created by Liyeyu on 2016/7/18.
 */
public class NovStoryApp extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        ImageLoader.init(this);
        startPlayService();
        BaseManager.init(this);
//        refWatcher = LeakCanary.install(this);
//        refWatcher.watch(this);
        CrashHandler.init(this);
        SupportUtilsConfig.init(this);
        RxHttpConfig.init(getApplicationContext(),Constants.BASE_URL);
    }

    public void startPlayService () {
        Intent it = new Intent (this, PlayControlService.class);
        startService(it);
    }

    public void stopPlayService () {
        Intent it = new Intent(this, PlayControlService.class);
        stopService(it);
    }
}
