package com.liyeyu.novstory.act;

import android.animation.ValueAnimator;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.liyeyu.novstory.BaseActivity;
import com.liyeyu.novstory.Constants;
import com.liyeyu.novstory.R;
import com.liyeyu.novstory.entry.MenuInfo;
import com.liyeyu.novstory.entry.SplashInfo;
import com.liyeyu.novstory.manager.AppConfig;
import com.liyeyu.novstory.utils.IntentUtils;
import com.liyeyu.novstory.view.AppBarHeadView;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

    private ViewPager mViewPager;
    private List<SplashInfo> mSplashList;
    private List<View> mViewList;
    private ValueAnimator mAnimator;

    @Override
    protected int OnCreateView() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return R.layout.activity_splash;
    }

    @Override
    protected void initView() {
        mViewPager = (ViewPager) findViewById(R.id.vp_splash);
        mViewPager.setAdapter(new SplashAdapter());
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setPageTransformer(false, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(View view, float position) {
//                int pageWidth = view.getWidth();
//                if (position < -1) {
////                    view.setAlpha(0);
//                } else if (position <= 1) { // [-1,1]
//                    TextView left = (TextView) view.findViewById(R.id.tv_splash_left);
//                    TextView right = (TextView) view.findViewById(R.id.tv_splash_right);
//                    left.setTranslationX((float) (-(1 - position) * 0.5 * pageWidth));
//                    right.setTranslationX( (-(1 - position) * pageWidth));
//
//                } else {
////                    view.setAlpha(0);
//                }
            }
        });
    }

    @Override
    protected void initData() {

        Boolean isFirst = AppConfig.get(Constants.FIRST_LAUNCH, true);
        if(!isFirst){
            startPlayListActivity();
        }
        mAnimator = ValueAnimator.ofFloat(1, 0.5f);
        mAnimator.setDuration(2000);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                if(value==0.5){
                    startPlayListActivity();
                }
            }
        });

        mSplashList = new ArrayList<>();
        mViewList = new ArrayList<>();
        mSplashList.add(new SplashInfo(getString(R.string.splash_left_1),getString(R.string.splash_right_1)));
        mSplashList.add(new SplashInfo(getString(R.string.splash_left_2),getString(R.string.splash_right_2)));
        mSplashList.add(new SplashInfo(getString(R.string.splash_left_3),getString(R.string.splash_right_3)));
        for (SplashInfo info:mSplashList) {
            View inflate = getLayoutInflater().inflate(R.layout.item_splash, null);
            TextView left = (TextView) inflate.findViewById(R.id.tv_splash_left);
            TextView right = (TextView) inflate.findViewById(R.id.tv_splash_right);
            left.setText(info.getLeft());
            right.setText(info.getRight());
            mViewList.add(inflate);
        }
    }

    @Override
    protected boolean isBindPlayService() {
        return false;
    }

    @Override
    protected AppBarHeadView initHeadView(List<MenuInfo> mMenus) {
        return null;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        View view = mViewList.get(position);
        TextView left = (TextView) view.findViewById(R.id.tv_splash_left);
        TextView right = (TextView) view.findViewById(R.id.tv_splash_right);
        left.setAlpha(1-positionOffset);
        right.setAlpha(1-positionOffset);
//        LogUtil.i("positionOffset"+positionOffset);
    }

    @Override
    public void onPageSelected(int position) {
        if(position==mViewList.size()-1){
            mAnimator.start();
        }else{
            mAnimator.cancel();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void startPlayListActivity(){
        AppConfig.save(Constants.FIRST_LAUNCH,false);
        IntentUtils.startPlayListActivity(SplashActivity.this);
        finish();
    }

    class SplashAdapter extends PagerAdapter{
        @Override
        public int getCount() {
            return mViewList.size();
        }
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = mViewList.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mViewList.get(position));
        }
    }

}
