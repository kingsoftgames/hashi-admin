package com.seasungames.hashiadmin.rollingupdate;

import com.seasungames.hashiadmin.aws.Aws;
import com.seasungames.hashiadmin.consul.Consul;

/**
 * Created by wangzhiguang on 2019-11-12.
 */
public interface Context {

    String autoScalingGroupName();

    Aws aws();

    Consul consul();
}
