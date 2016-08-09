package com.liyeyu.novstory.other.blur;

import android.animation.TypeEvaluator;

/**
 * Created by Joker on 2015/11/28.
 */
public class BlurEvaluator implements TypeEvaluator<Integer> {

  @Override
  public Integer evaluate(float fraction, Integer startValue, Integer endValue) {

    return (int) ((endValue - startValue) * fraction + startValue + 0.5f);
  }
}