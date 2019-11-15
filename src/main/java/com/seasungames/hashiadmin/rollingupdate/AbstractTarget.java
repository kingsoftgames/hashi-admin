package com.seasungames.hashiadmin.rollingupdate;

/**
 * Created by wangzhiguang on 2019-11-12.
 */
public abstract class AbstractTarget implements Target {

    protected Context context;

    @Override
    public void init(Context context) {
        this.context = context;
    }
}
