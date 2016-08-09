package com.liyeyu.novstory.act;

import com.liyeyu.novstory.BaseActivity;
import com.liyeyu.novstory.R;
import com.liyeyu.novstory.entry.MenuInfo;
import com.liyeyu.novstory.view.AppBarHeadView;

import java.util.List;

public class AboutActivity extends BaseActivity {

    @Override
    protected int OnCreateView() {
        return R.layout.activity_about;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {

    }

    @Override
    protected boolean isBindPlayService() {
        return false;
    }

    @Override
    protected AppBarHeadView initHeadView(List<MenuInfo> mMenus) {
        mHeadView = (AppBarHeadView) findViewById(R.id.head_about);
        mHeadView.setTitle(getString(R.string.app_about_our));
        mHeadView.setHeadBackShow(true);
        return mHeadView;
    }
}
