package com.liyeyu.novstory.utils;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

/**
 * Created by Liyeyu on 2016/7/19.
 */
public class TimeUtils {
    public static String defPattern = "00";
    public static DecimalFormat defFormat = new DecimalFormat(defPattern);

    private static String format(long s,String pattern){
        defFormat.applyPattern(pattern);
        return defFormat.format(s);
    }
    private static String format(long s){
        return format(s,defPattern);
    }

    public static String computer(long s, TimeUnit unit){
        String time;
        if(unit==TimeUnit.MILLISECONDS){
            s = s / 1000;
        }
        int min = (int) (s/60);
        s = (int) (s%60);
        if(min<60){
            time = format(min)+":"+format(s);
        }else{
            int hour = min/60;
            min = min%60;
            time = format(hour)+":"+format(min)+":"+format(s);
        }
        return time;
    }
}
