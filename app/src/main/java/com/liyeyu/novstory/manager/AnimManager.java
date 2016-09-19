package com.liyeyu.novstory.manager;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

import com.liyeyu.novstory.manager.base.BaseManager;

/**
 * Created by Liyeyu on 2016/9/12.
 */
public class AnimManager extends BaseManager{

    public static void animLrc(final float from,final float to,final View view){
        view.clearAnimation();
        AlphaAnimation alpha = new AlphaAnimation(from,to);
        alpha.setDuration(500);
        alpha.setInterpolator(new LinearInterpolator());
        alpha.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(to>from){
                    view.setVisibility(View.VISIBLE);
                }else{
                    view.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(alpha);
    }
}
