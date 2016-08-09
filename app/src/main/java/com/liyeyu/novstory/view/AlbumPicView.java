package com.liyeyu.novstory.view;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ViewFlipper;

import com.liyeyu.novstory.R;
import com.liyeyu.novstory.events.MusicFlingEvent;
import com.liyeyu.novstory.events.RxBus;
import com.liyeyu.novstory.utils.ImageLoader;

import rx.Subscription;
import rx.functions.Action1;


/**
 * Created by Liyeyu on 2016/7/20.
 */
public class AlbumPicView extends RelativeLayout{

    public static final int ANIM_START = 0;
    public static final int ANIM_END = 1;
    private int currentPos = 0;
    private boolean isFling = false;
    private ImageView[] mImageViews = new ImageView[3];
    private ImageView mIv1;
    private ImageView mIv2;
    private ImageView mIv3;
    private Animation mLeftAnim;
    private Animation mRightAnim;

    private ViewFlipper mFlipper;

    private onFlingListener mFlingListener;
    private Subscription mRxMusicFling;

    public AlbumPicView(Context context) {
        super(context);
        initAlbumPager(context);
    }
    public AlbumPicView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAlbumPager(context);
    }
    public void setFlingListener(onFlingListener flingListener) {
        mFlingListener = flingListener;
    }

    private void initAlbumPager(Context context) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.album_pic_view,this);
        mFlipper = (ViewFlipper) inflate.findViewById(R.id.vf_album);
        mIv1 = (ImageView) inflate.findViewById(R.id.iv_album_1);
        mIv2 = (ImageView) inflate.findViewById(R.id.iv_album_2);
        mIv3 = (ImageView) inflate.findViewById(R.id.iv_album_3);
        mImageViews[0] = mIv1;
        mImageViews[1] = mIv2;
        mImageViews[2] = mIv3;
        currentPos = 0;
        initAnim();
        mRxMusicFling = RxBus.get().register(MusicFlingEvent.class, new Action1<MusicFlingEvent>() {
            @Override
            public void call(MusicFlingEvent musicFlingEvent) {
                if (musicFlingEvent != null) {
                    fling(musicFlingEvent.state);
                }
            }
        });
    }

    public void recycle(){
        mRxMusicFling.unsubscribe();
    }

    private void initAnim() {
        mLeftAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anim_album_in_left);
        mRightAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anim_album_in_right);
        mLeftAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if(mFlingListener!=null&& !isFling){
                    isFling = true;
                    mFlingListener.showNext(ANIM_START);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showNext(ANIM_END);
                isFling = false;

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mRightAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if(mFlingListener!=null && !isFling){
                    isFling = true;
                    mFlingListener.showPrevious(ANIM_START);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showPrevious(ANIM_END);
                isFling = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    public void fling(int state){
        if(state==MusicFlingEvent.FLING_LEFT){
            leftFling();
            if(!isFling){
                mFlipper.showNext();
            }
        }else if(state==MusicFlingEvent.FLING_RIGHT){
            rightFling();
            if(!isFling){
                mFlipper.showPrevious();
            }
        }
    }

    public void leftFling(){
        mFlipper.setInAnimation(mLeftAnim);
        mFlipper.setOutAnimation(getContext(),R.anim.anim_album_out_left);
    }
    public void rightFling(){
        mFlipper.setInAnimation(mRightAnim);
        mFlipper.setOutAnimation(getContext(),R.anim.anim_album_out_right);
    }

    public void showNext(int animStart) {
        if(isFling){
            mFlipper.stopFlipping();
            currentPos = Math.abs(++currentPos % mImageViews.length);
            if(mFlingListener!=null){
                mFlingListener.showNext(animStart);
            }
        }
    }
    public void showPrevious(int animEnd) {
        if(isFling){
            mFlipper.stopFlipping();
            currentPos = Math.abs((--currentPos+mImageViews.length)) % mImageViews.length;
            if(mFlingListener!=null){
                mFlingListener.showPrevious(animEnd);
            }
        }
    }

    public ImageView getCurrentImageView(){
       return mImageViews[currentPos];
    }
    public int getCurrentIndex(){
       return currentPos;
    }

    public int getPreIndex(){
        int pre;
        if(currentPos==0){
            pre = getSize()-1;
        }else {
            pre = currentPos-1;
        }
        return pre;
    }
    public int getNextIndex(){
        int next;
        if(currentPos==getSize()-1){
            next = 0;
        }else {
            next = currentPos+1;
        }
        return next;
    }
    public int getSize(){
       return mImageViews.length;
    }
    public ImageView getImageView(int pos){
        if(pos<0){
            pos = 0;
        }
        if(pos>mImageViews.length-1){
            pos = mImageViews.length-1;
        }
       return mImageViews[pos];
    }
    public void setImage(int pos,Uri path){
        ImageView view = getImageView(pos);
        ImageLoader.get().load(view,path);
    }
    public void setImage(int pos,long songId, long albumId){
        ImageView view = getImageView(pos);
        ImageLoader.get().load(view,songId, albumId);
    }

    public interface onFlingListener{
       void showPrevious(int state);
       void showNext(int state);
    }
}
