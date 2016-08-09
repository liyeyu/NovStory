package com.liyeyu.novstory.entry;

import java.util.List;

/**
 * Created by Administrator on 2016/5/14.
 */
public class MenuInfo {

    private int id;
    private String name;
    private int icon;
    private List<MenuInfo> mMenuSubs;


    public MenuInfo(int id, String name, int icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public List<MenuInfo> getMenuSubs() {
        return mMenuSubs;
    }

    public void setMenuSubs(List<MenuInfo> menuSubs) {
        mMenuSubs = menuSubs;
    }
}
