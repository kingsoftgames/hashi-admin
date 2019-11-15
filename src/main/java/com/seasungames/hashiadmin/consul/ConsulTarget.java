package com.seasungames.hashiadmin.consul;

import com.seasungames.hashiadmin.rollingupdate.AbstractTarget;
import software.amazon.awssdk.services.autoscaling.model.Instance;

import java.util.List;

/**
 * Created by wangzhiguang on 2019-11-12.
 */
public final class ConsulTarget extends AbstractTarget {

    @Override
    public void sortInstances(List<Instance> instances) {
    }

    @Override
    public void prepareNewInstance(Instance newInstance) {
    }

    @Override
    public void retireOldInstance(Instance oldInstance) {
    }
}
