package com.liyeyu.novstory.act;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.liyeyu.novstory.BaseActivity;
import com.liyeyu.novstory.Constants;
import com.liyeyu.novstory.R;
import com.liyeyu.novstory.adapter.MusicRecycleAdapter;
import com.liyeyu.novstory.adapter.MusicViewHolder;
import com.liyeyu.novstory.adapter.callback.MusicItemDecoration;
import com.liyeyu.novstory.adapter.callback.MusicItemTouchHelperCallback;
import com.liyeyu.novstory.adapter.callback.OnMusicItemClick;
import com.liyeyu.novstory.entry.Audio;
import com.liyeyu.novstory.entry.MenuInfo;
import com.liyeyu.novstory.events.MusicChangeEvent;
import com.liyeyu.novstory.events.ProgressUpdateEvent;
import com.liyeyu.novstory.events.RxBus;
import com.liyeyu.novstory.play.MediaQueueManager;
import com.liyeyu.novstory.play.NovPlayController;
import com.liyeyu.novstory.utils.ImageLoader;
import com.liyeyu.novstory.utils.IntentUtils;
import com.liyeyu.novstory.view.AppBarHeadView;
import com.liyeyu.novstory.view.MusicPlayView;

import java.util.ArrayList;
import java.util.List;

import liyeyu.support.utils.manager.PermissionsManager;
import liyeyu.support.utils.utils.OtherUtil;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MusicListActivity extends BaseActivity{

    private RecyclerView mRecyclerView;
    private MusicRecycleAdapter mMusicRecycleAdapter;
    private List<Audio> mMedias = new ArrayList<>();
    private List<Audio> mSearch = new ArrayList<>();
    private ItemTouchHelper mTouchHelper;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private SearchView mSearchView;
    private MusicPlayView mPlayingView;
    private PlaybackStateCompat mPlaybackState;
    private MediaMetadataCompat mMetadata;
    private SearchView.SearchAutoComplete mSearchEdit;
    private Subscription mRxMusicChange;
    private Subscription mRxProgressUpdate;
    private PermissionsManager.CheckCallBack mPermissionsCallBack;
    private TextView mHeadName;
    private String mTag;
    private ImageView mMaskView;
    private ValueAnimator mAnimator;

    @Override
    protected int OnCreateView() {
        mRxMusicChange = RxBus.get().register(MusicChangeEvent.class, new Action1<MusicChangeEvent>() {
            @Override
            public void call(MusicChangeEvent changeEvent) {
                updatePlayView(changeEvent);
            }
        });
        mRxProgressUpdate = RxBus.get().register(ProgressUpdateEvent.class, new Action1<ProgressUpdateEvent>() {
            @Override
            public void call(ProgressUpdateEvent progressUpdateEvent) {
                if(mPlayingView!=null){
                    mPlayingView.setProgress(progressUpdateEvent.getProgress());
                }
            }
        });
        return R.layout.activity_music_list;
    }

    @Override
    protected void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_music_list);
        mPlayingView = (MusicPlayView) findViewById(R.id.mpv_playing);
        mMaskView = (ImageView) findViewById(R.id.iv_list_mask);

        mMusicRecycleAdapter = new MusicRecycleAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new MusicItemDecoration(1));
//        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        MusicItemTouchHelperCallback callback = new MusicItemTouchHelperCallback(mMusicRecycleAdapter);
        mTouchHelper = new ItemTouchHelper(callback);
        mTouchHelper.attachToRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(mMusicRecycleAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                listAnim(newState!=RecyclerView.SCROLL_STATE_IDLE);
            }
        });
        initNav();
    }
    @Override
    protected void onResume() {
        super.onResume();
        updatePlayView(null);
    }


    private void listAnim(final boolean isShowMask){
        mAnimator = ValueAnimator.ofFloat(1, 0.4f);
        mAnimator.setDuration(1000);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                if(isShowMask){
                    mRecyclerView.setAlpha(value);
                }else{
                    mRecyclerView.setAlpha(0.6f+value);
                }
            }
        });
        mAnimator.start();
    }

    private void updatePlayView(MusicChangeEvent changeEvent) {
        mMediaController = getSupportMediaController();
        if(mMediaController!=null){
            mMetadata = mMediaController.getMetadata();
        }
        if(mMetadata!=null){
            mPlayingView.setVisibility(View.VISIBLE);
            mPlayingView.mediaChange(mMetadata);
            long songId = Long.parseLong(mMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
            long albumId = mMetadata.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER);
            mPlayingView.setIcon(songId,albumId);
            mPlayingView.setTitle(mMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            mPlayingView.setSinger(mMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
            mPlaybackState = mMediaController.getPlaybackState();
            mPlayingView.setPlay(mPlaybackState.getState()==PlaybackStateCompat.STATE_STOPPED
                    ||mPlaybackState.getState()==PlaybackStateCompat.STATE_PAUSED);
        }
        if(changeEvent!=null){
            if(changeEvent.isRemove){
                mPlayingView.setVisibility(View.GONE);
            }
            mPlayingView.setPlay(changeEvent.getState()==PlaybackStateCompat.STATE_STOPPED
            ||changeEvent.getState()==PlaybackStateCompat.STATE_PAUSED);
        }
    }

    private void initNav() {
        mNavigationView = (NavigationView) findViewById(R.id.list_nav);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.dl_list_root);
        View mHeaderView = mNavigationView.inflateHeaderView(R.layout.nav_list_head);
        mNavigationView.inflateMenu(R.menu.menu_nav_item);
        onNavItemSelected(mNavigationView);
        mHeadName = (TextView)mHeaderView.findViewById(R.id.tv_home_name);

        String html = getString(R.string.name_my)+"<img src='play_icn_love'/>"+getString(R.string.name_you);
        if(mHeadName !=null){
            mHeadName.setText(Html.fromHtml(html, new Html.ImageGetter() {
                @Override
                public Drawable getDrawable(String s) {
                    int drawableId = getResources().getIdentifier(s, "drawable", getPackageName());
                    Drawable drawable = ContextCompat.getDrawable(MusicListActivity.this,drawableId);
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                    return drawable;
                }
            }, null));
        }

        mToggle = new ActionBarDrawerToggle(this,mDrawerLayout,mHeadView.getToolbar(), R.string.open,R.string.close);
        mDrawerLayout.setStatusBarBackgroundColor(ContextCompat.getColor(this,R.color.colorPrimary));
        mToggle.syncState();
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener(){

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                OtherUtil.hideSoftInputFromWindow(MusicListActivity.this,mSearchView);
            }
        });
        mSearchView = new SearchView(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void onNavItemSelected(NavigationView mNav) {
        mNav.setCheckedItem(R.id.nav_menu_home);
        mNav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                switch (menuItem.getItemId()) {
                    case R.id.nav_menu_home:
                        updateMediaList(MediaQueueManager.ALL,false);
                        break;
                    case R.id.nav_menu_love:
                        updateMediaList(MediaQueueManager.LOVE,false);
                        break;
                    case R.id.nav_menu_about:
                        IntentUtils.startAboutActivity(MusicListActivity.this);
                        break;
                    case R.id.nav_menu_setting:
//                        IntentUtils.startSettingActivity(MusicListActivity.this);
                        break;
                }
                // Menu item点击后选中，并关闭DrawerLayout
                return true;
            }
        });
    }

    @Override
    protected void initData() {
        mMediaController = getSupportMediaController();
        updateMediaList(MediaQueueManager.ALL,true);
    }

    private void updateMediaList(final String tag,final boolean init){
        mPermissionsCallBack = new PermissionsManager.CheckCallBack() {
            @Override
            public void onSuccess(String permission) {
                loading(true);
                Observable.create(new Observable.OnSubscribe<List<Audio>>() {
                    @Override
                    public void call(Subscriber<? super List<Audio>> subscriber) {
                        mTag = tag;
                        mMedias = MediaQueueManager.get().queryMediaList(MusicListActivity.this, tag);
                        if(init)
                            NovPlayController.get().setConfigMetaDate();
                        subscriber.onNext(mMedias);
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<List<Audio>>() {
                            @Override
                            public void call(List<Audio> audios) {
                                loading(false);
                                updatePlayView(null);
                                mMusicRecycleAdapter.updateData(audios);
                            }
                        });
            }

            @Override
            public void onError(String permission) {

            }
        };
        PermissionsManager.get().checkPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE,mPermissionsCallBack);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //检验权限
        PermissionsManager.get().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRxMusicChange.unsubscribe();
        mRxProgressUpdate.unsubscribe();
        ImageLoader.get().release();
        if(mAnimator!=null){
            mAnimator.cancel();
        }
    }

    @Override
    protected boolean isBindPlayService() {
        return true;
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void initEvent() {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mMusicRecycleAdapter.setScrollState(dy>0?MusicRecycleAdapter.SCROLL_STATE_DOWN:MusicRecycleAdapter.SCROLL_STATE_UP);
            }
        });
        mMusicRecycleAdapter.setItemClick(new OnMusicItemClick() {
            @Override
            public void onItemClick(MusicViewHolder viewHolder, int pos) {
                IntentUtils.startPlayActivity(MusicListActivity.this,mMusicRecycleAdapter.getData().get(pos));
                NovPlayController.get().play(mMusicRecycleAdapter.getData().get(pos));
            }

            @Override
            public void onItemOptionsClick(View view, int pos) {
                //TODO
            }
        });
    }

    @Override
    protected AppBarHeadView initHeadView(List<MenuInfo> mMenus) {
        mHeadView = (AppBarHeadView) findViewById(R.id.head_music_list);
        mMenus.add(new MenuInfo(R.string.search,getString(R.string.search),R.drawable.actionbar_search));
        mHeadView.setHeadBackShow(false);
        return mHeadView;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean b = super.onPrepareOptionsMenu(menu);
        menu.findItem(R.string.search).setActionView(initSearchView());
        return b ;
    }

    private SearchView initSearchView() {
        ImageView mIcon = (ImageView) mSearchView.findViewById(R.id.search_button);
        mIcon.setImageResource(R.drawable.actionbar_search);
        mSearchEdit = (SearchView.SearchAutoComplete) mSearchView.findViewById(R.id.search_src_text);
        mSearchEdit.setTextColor(ContextCompat.getColor(this,R.color.white));
        mSearchEdit.setHintTextColor(ContextCompat.getColor(this,R.color.white_alpha_dd));
        mSearchEdit.setTextSize(12);
        ImageView closeImg = (ImageView) mSearchView.findViewById(R.id.search_close_btn);
        closeImg.setImageResource(R.drawable.search_close);

        mSearchView.setQueryHint(getString(R.string.app_name));
        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                isClose = true;
                mMusicRecycleAdapter.updateData(mMedias);
                return false;
            }
        });
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!TextUtils.isEmpty(newText)){
                    mSearch.clear();
                    for (Audio item:mMedias) {
                        if(item.getTitle().startsWith(newText)){
                            mSearch.add(item);
                        }
                    }
                    mMusicRecycleAdapter.updateData(mSearch);
                }else{
                    mMusicRecycleAdapter.updateData(mMedias);
                }
                return true;
            }
        });
        return mSearchView;
    }

    boolean isClose = false;
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== Constants.LOVE_STATE_CHANGE
                && resultCode==RESULT_OK && MediaQueueManager.LOVE.equals(mTag)){
            updateMediaList(MediaQueueManager.LOVE,false);
        }
    }
}
