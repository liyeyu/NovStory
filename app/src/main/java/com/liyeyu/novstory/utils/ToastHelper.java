package com.liyeyu.novstory.utils;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Liyeyu on 2016/7/19.
 */
public class ToastHelper {

    private static Toast mToast;
    private static String oldMsg;
    private static long oneTime = 0;
    private static long twoTime = 0;
    /**
     * 避免吐司多次弹出
     *
     * @param context
     * @param s
     */
    public static void showToast(Context context, String s) {
        if (mToast == null) {
            mToast = Toast.makeText(context, s, Toast.LENGTH_SHORT);
            mToast.show();
            oneTime = System.currentTimeMillis();
        } else {
            twoTime = System.currentTimeMillis();
            if (s.equals(oldMsg)) {
                if (twoTime - oneTime > Toast.LENGTH_SHORT) {
                    mToast.show();
                }
            } else {
                oldMsg = s;
                mToast.setText(s);
                mToast.show();
            }
        }
        oneTime = twoTime;
    }

    public static void show(Context context,String msg){
        Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
    }
    public static void show(View view, String msg){
        Snackbar.make(view,msg,Snackbar.LENGTH_LONG).show();
    }

}
