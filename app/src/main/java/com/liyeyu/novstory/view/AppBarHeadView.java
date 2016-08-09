package com.liyeyu.novstory.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.liyeyu.novstory.R;


/**通用的标题栏
 * Created by liyeyu on 2016/5/14.
 */
public class AppBarHeadView extends FrameLayout {

    private View mAppBarLayout;
    private Toolbar mToolbar;
    private TextView mTitle;
    private TextView mSubTitle;

    public boolean isHeadBackShow() {
        return headBackShow;
    }

    public void setHeadBackShow(boolean headBackShow) {
        this.headBackShow = headBackShow;
    }

    private boolean headBackShow = false;

    public AppBarHeadView(Context context) {
        super(context);
        initView(context,null);
    }

    public AppBarHeadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context,attrs);
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    public View getAppBarLayout() {
        return mAppBarLayout;
    }

    private void initView(Context context, AttributeSet attrs) {
        mAppBarLayout =  LayoutInflater.from(context).inflate(R.layout.head_toolbar, this);
        mToolbar = (Toolbar) mAppBarLayout.findViewById(R.id.head_toolbar);
        mToolbar.setTitle("");
        mTitle = (TextView) mAppBarLayout.findViewById(R.id.tv_head_title);
        mSubTitle = (TextView) mAppBarLayout.findViewById(R.id.tv_head_sub_title);
        if(attrs!=null){
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.app_bar);
            int color = a.getColor(R.styleable.app_bar_toolbar_bg, context.getResources().getColor(R.color.colorPrimary));
            mToolbar.setBackgroundColor(color);
            a.recycle();
        }
        mSubTitle.setVisibility(View.GONE);
    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }
    public void setSubTitle(String title) {
        mSubTitle.setText(title);
        mSubTitle.setVisibility(TextUtils.isEmpty(title)?View.GONE:View.VISIBLE);
    }
    public void setSubTitleColor(int color) {
        mSubTitle.setTextColor(color);
    }
    public void setTitleColor(int color) {
        mTitle.setTextColor(color);
    }
    public void setTitleSize(int size) {
        mTitle.setTextSize(size);
    }
}
