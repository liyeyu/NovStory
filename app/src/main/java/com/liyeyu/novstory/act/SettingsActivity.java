package com.liyeyu.novstory.act;

import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.liyeyu.novstory.BaseActivity;
import com.liyeyu.novstory.R;
import com.liyeyu.novstory.entry.MenuInfo;
import com.liyeyu.novstory.manager.AppConfig;
import com.liyeyu.novstory.view.AppBarHeadView;

import java.util.List;

public class SettingsActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {

    private CheckBox mOpenWin;

    @Override
    protected int OnCreateView() {
        return R.layout.activity_settings;
    }

    @Override
    protected void initView() {
        mOpenWin = (CheckBox) findViewById(R.id.setting_window);
        mOpenWin.setOnCheckedChangeListener(this);
        mOpenWin.setChecked(AppConfig.get("isCheck",true));
    }

    @Override
    protected void initData() {

    }

    @Override
    protected boolean isBindPlayService() {
        return true;
    }

    @Override
    protected AppBarHeadView initHeadView(List<MenuInfo> mMenus) {
        mHeadView = (AppBarHeadView) findViewById(R.id.head_setting);
        mHeadView.setTitle(getString(R.string.title_settings));
        mHeadView.setHeadBackShow(true);
        return mHeadView;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        AppConfig.save("isCheck",isChecked);
        mPlayService.updateWindow(isChecked);
    }
}
