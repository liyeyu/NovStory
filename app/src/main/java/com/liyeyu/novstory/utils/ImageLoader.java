package com.liyeyu.novstory.utils;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import com.liyeyu.novstory.R;
import com.liyeyu.novstory.other.blur.BlurTarget;
import com.liyeyu.novstory.other.blur.PicassoTransformation;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

/**
 * Created by Liyeyu on 2016/7/19.
 */
public class ImageLoader {

    public static int DEF_BG = R.drawable.img_default;
    public static int DEF_W = 400;
    public static int DEF_H = 400;
    private static Picasso picasso;
    private static ImageLoader loader = new ImageLoader();
    private static PicassoTransformation transformation;
    private static Application mContext;
    public static LruCache mLruCache;
    private Bitmap DEF_BITMAP;

    private ImageLoader() {
    }

    public static ImageLoader get(){
        return loader;
    }

    public static void init(Application context){
        mContext = context;
        picasso = Picasso.with(context);
        transformation = new PicassoTransformation(mContext,PicassoTransformation.BLUR_RADIUS);
        mLruCache = new LruCache(mContext);
    }

    public Bitmap getDefBitmap(){
        if(DEF_BITMAP==null){
            BitmapFactory.Options ops = new BitmapFactory.Options();
            ops.inJustDecodeBounds = true;
            ops.inSampleSize = ImageLoader.calculateInSampleSize(ops,100,100);
            BitmapFactory.decodeResource(mContext.getResources(),ImageLoader.DEF_BG,ops);
            ops.inJustDecodeBounds = false;
            DEF_BITMAP = BitmapFactory.decodeResource(mContext.getResources(),ImageLoader.DEF_BG,ops);
        }
        return DEF_BITMAP;
    }

    public void load(ImageView view, int res){
        picasso.load(res)
                .resize(50,50)
                .into(view);
    }
    public void load(ImageView view,long songId, long albumId){
       if(view==null){
           return;
       }
        Bitmap bitmap = mLruCache.get(String.valueOf(songId));
        if(bitmap==null){
            bitmap = MediaUtils.getArtwork(mContext,songId,albumId);
            mLruCache.set(String.valueOf(songId),bitmap);
        }
        view.setImageBitmap(bitmap);
    }
    public Bitmap load(long songId, long albumId){
       Bitmap bitmap = mLruCache.get(String.valueOf(songId));
        if(bitmap==null){
            bitmap = MediaUtils.getArtwork(mContext,songId,albumId);
            mLruCache.set(String.valueOf(songId),bitmap);
        }
        return bitmap;
    }
    public void load(ImageView view, Uri path){
        picasso.load(path)
                .error(ImageLoader.DEF_BG)
                .resize(200,200)
                .tag(new BlurTarget(view,ImageLoader.DEF_BG))
                .into(view);
    }
    public void blur(ImageView view,int res){
        picasso.load(res)
                .resize(100,100)
                .centerCrop()
                .transform(transformation)
                .tag(new BlurTarget(view,res))
//                .memoryPolicy(NO_CACHE, NO_STORE)
                .into(view);
    }
    public void blur(final ImageView view,final Uri res){
        picasso.load(res)
                .resize(50, 50)
                .centerCrop()
                .transform(transformation)
                .into(view);

    }
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            while ((height / inSampleSize) >= reqHeight
                    && (width / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
    public void release(){
        if(DEF_BITMAP!=null){
            DEF_BITMAP = null;
        }
        mLruCache.clear();
    }
}
