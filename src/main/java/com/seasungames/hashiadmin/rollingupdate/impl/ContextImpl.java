package com.seasungames.hashiadmin.rollingupdate.impl;

import com.seasungames.hashiadmin.aws.Aws;
import com.seasungames.hashiadmin.consul.Consul;
import com.seasungames.hashiadmin.nomad.Nomad;
import com.seasungames.hashiadmin.rollingupdate.Context;
import software.amazon.awssdk.regions.Region;

/**
 * Created by wangzhiguang on 2019-11-12.
 */
public final class ContextImpl implements Context {

    private final String asgName;
    private final Aws aws;
    private final Consul consul;
    private final Nomad nomad;

    public ContextImpl(String asgName, Region region) {
        this.asgName = asgName;
        this.aws = Aws.create(region);
        this.consul = Consul.create();
        this.nomad = Nomad.create();
    }

    @Override
    public String autoScalingGroupName() {
        return this.asgName;
    }

    @Override
    public Aws aws() {
        return this.aws;
    }

    @Override
    public Consul consul() {
        return this.consul;
    }

    @Override
    public Nomad nomad() {
        return this.nomad;
    }
}
