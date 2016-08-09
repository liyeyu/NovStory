package com.liyeyu.novstory.entry;

/**
 * Created by Liyeyu on 2016/8/4.
 */
public class SplashInfo {
   private String left;
   private String right;

    public SplashInfo(String left, String right) {
        this.left = left;
        this.right = right;
    }
    public SplashInfo() {
    }

    public String getLeft() {
        return left;
    }


    public String getRight() {
        return right;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public void setRight(String right) {
        this.right = right;
    }
}
