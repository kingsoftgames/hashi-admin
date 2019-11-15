package com.seasungames.hashiadmin.rollingupdate;

import software.amazon.awssdk.services.autoscaling.model.Instance;

import java.util.List;

/**
 * Created by wangzhiguang on 2019-11-12.
 */
public interface Target {

    void init(Context context);

    void sortInstances(List<Instance> instances);

    void prepareNewInstance(Instance newInstance);

    void retireOldInstance(Instance oldInstance);
}
