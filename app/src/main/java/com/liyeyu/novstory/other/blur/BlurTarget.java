package com.liyeyu.novstory.other.blur;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by Joker on 2015/11/27.
 */
public class BlurTarget implements Target {
  ImageView mView;
  int res;
  public BlurTarget(ImageView view,int res) {
    mView = view;
    this.res = res;
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  @Override
  public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
    ValueAnimator blurAnimator = ValueAnimator.ofInt(1, 4);
    blurAnimator.setEvaluator(new BlurEvaluator());
    blurAnimator.setDuration(100);
    blurAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        mView.setImageBitmap(quickBlur(bitmap,(Integer) animation.getAnimatedValue()));
      }
    });
    blurAnimator.start();
  }

  @Override
  public void onBitmapFailed(Drawable errorDrawable) {
    mView.setImageResource(res);
  }

  @Override
  public void onPrepareLoad(Drawable placeHolderDrawable) {

  }
  private Bitmap quickBlur(Bitmap sourceBitmap, int factor) {
    if (factor <= 0) {
      return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
    }

    return Bitmap.createScaledBitmap(sourceBitmap, sourceBitmap.getWidth() / factor,
            sourceBitmap.getHeight() / factor, true);
  }
}
