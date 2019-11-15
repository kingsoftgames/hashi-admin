package com.seasungames.hashiadmin.aws;

import com.seasungames.hashiadmin.aws.impl.AwsImpl;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.autoscaling.model.AutoScalingInstanceDetails;
import software.amazon.awssdk.services.autoscaling.model.Instance;

import java.util.List;
import java.util.Optional;

/**
 * Created by wangzhiguang on 2019-11-13.
 */
public interface Aws {

    static Aws create(Region region) {
        return new AwsImpl(region);
    }

    Optional<AutoScalingInstanceDetails> getInstanceAsg(String instanceId);

    List<Instance> listAsgInstances(String asgName);

    void detachInstance(String instanceId, String asgName);

    void terminateInstance(String instanceId);
}
