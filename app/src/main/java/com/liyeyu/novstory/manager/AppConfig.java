package com.liyeyu.novstory.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.liyeyu.novstory.Constants;
import com.liyeyu.novstory.manager.base.BaseManager;

/**
 * Created by Liyeyu on 2016/7/25.
 */
public class AppConfig extends BaseManager{

    public static SharedPreferences sp;
    public static final String CONFIG_NAME = Constants.PACKAGE_NAME +"_config";

    static {
        sp =  mApp.getSharedPreferences(CONFIG_NAME, Context.MODE_PRIVATE);
    }

    public static <T> void save(String key,T t){
        if(t==null){
            return;
        }
        if(t instanceof String){
            sp.edit().putString(key,t.toString()).commit();
        }
        if(t instanceof Integer){
            sp.edit().putInt(key,(Integer)t).commit();
        }
        if(t instanceof Long){
            sp.edit().putLong(key,(Long)t).commit();
        }
        if(t instanceof Double || t instanceof Float){
            sp.edit().putFloat(key,(Float)t).commit();
        }
        if(t instanceof Boolean){
            sp.edit().putBoolean(key,(Boolean)t).commit();
        }
    }

    public static <R> R get(String key,R defValue){
        Object obj = null;
        if(defValue instanceof String){
            obj = sp.getString(key,defValue.toString());
        }
        if(defValue instanceof Integer){
            obj = sp.getInt(key,(Integer)defValue);
        }
        if(defValue instanceof Long){
            obj = sp.getLong(key,(Long)defValue);
        }
        if(defValue instanceof Double || defValue instanceof Float){
            obj = sp.getFloat(key,(Float)defValue);
        }
        if(defValue instanceof Boolean){
            obj = sp.getBoolean(key,(Boolean)defValue);
        }
        return (R)obj;
    }
}
