package com.liyeyu.novstory;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.liyeyu.novstory.entry.MenuInfo;
import com.liyeyu.novstory.play.PlayControlService;
import com.liyeyu.novstory.view.AppBarHeadView;
import com.liyeyu.novstory.view.MusicPlayView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Liyeyu on 2016/7/18.
 */
public abstract class BaseActivity extends AppCompatActivity implements ServiceConnection{

    protected AppBarHeadView mHeadView;
    protected List<MenuInfo> mMenus = new ArrayList<>();
    protected MusicPlayView mMusicPlayView;
    protected ViewGroup mRoot;
    private View mLoading;
    protected NovStoryApp mApp;
    protected MediaControllerCompat mMediaController;
    protected PlayControlService.IPlayBinder mPlayService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(OnCreateView());
        if(isBindPlayService()){
            bindPlayService();
        }else{
            initData();
            initHead();
            initView();
            initEvent();
        }
        mRoot = (ViewGroup) getWindow().getDecorView().findViewById(android.R.id.content);
        mApp = (NovStoryApp) getApplication();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isBindPlayService()){
            unbindPlayService();
        }
    }
    protected void initEvent(){}
    protected abstract int OnCreateView();
    protected abstract void initView();
    protected abstract void initData();
    protected abstract boolean isBindPlayService();

    protected abstract AppBarHeadView initHeadView(List<MenuInfo> mMenus);

    protected void removePlayBottom(){
        if(mMusicPlayView==null||mRoot==null){
            return;
        }
        mRoot.removeView(mMusicPlayView);
    }
    protected void loading(boolean show){
        if(mLoading!=null){
            mLoading.setVisibility(show?View.VISIBLE:View.GONE);
            return;
        }
        mLoading = getLayoutInflater().inflate(R.layout.loading_view, null);
        mRoot.addView(mLoading);
    }

    /**
     * 每个activity自动添加标题
     */
    public void initHead() {
        mHeadView = initHeadView(mMenus);
        if (mHeadView != null) {
            mHeadView.setTitleSize(17);
            setSupportActionBar(mHeadView.getToolbar());
            setHeadBackShow(mHeadView.isHeadBackShow());
        }
    }
    /**
     * 是否显示回退键
     *
     * @param isShow
     */
    protected void setHeadBackShow(boolean isShow) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(isShow);
        getSupportActionBar().setHomeButtonEnabled(isShow);
        if (mHeadView != null && isShow) {
            mHeadView.getToolbar().setNavigationIcon(R.drawable.actionbar_back);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_head, menu);
//        MenuItem home = menu.findItem(android.R.id.home);
//        View actionView = MenuItemCompat.getActionView(home);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        for (MenuInfo item : mMenus) {
            menu.add(0, item.getId(), 0, item.getName()).setIcon(item.getIcon()).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void bindPlayService () {
        Intent it = new Intent (this, PlayControlService.class);
        this.bindService(it, this, Service.BIND_AUTO_CREATE);
    }

    public void unbindPlayService () {
        this.unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mPlayService = (PlayControlService.IPlayBinder) iBinder;
        MediaSessionCompat.Token token = mPlayService.getPlayServiceToken();
        try {
            mMediaController = new MediaControllerCompat(this,token);
            setSupportMediaController(mMediaController);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if(isBindPlayService()){
            initData();
            initHead();
            initView();
            initEvent();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        super.startActivityForResult(intent, requestCode, options);
        //转场动画
        overridePendingTransition(R.anim.push_right_in,R.anim.hold);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.hold,R.anim.push_right_out);
    }

}
