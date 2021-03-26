package com.seasungames.hashiadmin.aws.impl;

import com.seasungames.hashiadmin.aws.Aws;
import com.seasungames.hashiadmin.aws.AwsException;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.*;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceStatusRequest;
import software.amazon.awssdk.services.ec2.model.InstanceStateChange;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.seasungames.hashiadmin.Utils.sleep;

/**
 * Created by wangzhiguang on 2019-11-13.
 */
@Slf4j
public final class AwsImpl implements Aws {

    private final Ec2Client ec2;
    private final AutoScalingClient autoScaling;

    public AwsImpl(Region region) {
        this.ec2 = Ec2Client.builder().region(region).build();
        this.autoScaling = AutoScalingClient.builder().region(region).build();
    }

    @Override
    public Optional<AutoScalingInstanceDetails> getInstanceAsg(String instanceId) {
        var request = DescribeAutoScalingInstancesRequest.builder()
            .instanceIds(instanceId)
            .build();
        var response = autoScaling.describeAutoScalingInstances(request);
        var instances = response.autoScalingInstances();
        return instances.isEmpty() ? Optional.empty() : Optional.of(instances.get(0));
    }

    @Override
    public List<Instance> listAsgInstances(String asgName) {
        var request = DescribeAutoScalingGroupsRequest.builder()
            .autoScalingGroupNames(asgName)
            .build();
        var response = autoScaling.describeAutoScalingGroups(request);
        if (response.autoScalingGroups().isEmpty()) {
            throw new AwsException("Unable to find asg " + asgName);
        }
        var asg = response.autoScalingGroups().get(0);
        return new ArrayList<>(asg.instances());
    }

    @Override
    public void detachInstance(String instanceId, String asgName) {
        var activity = detachInstanceFromAsg(instanceId, asgName);
        pollScalingActivity(activity.activityId());
    }

    @Override
    public void terminateInstance(String instanceId) {
        var request = TerminateInstancesRequest.builder()
            .instanceIds(instanceId)
            .build();
        var response = ec2.terminateInstances(request);
        InstanceStateChange isc = response.terminatingInstances().get(0);
        log.info("Instance {} changed from {} to {}", isc.instanceId(), isc.previousState(), isc.currentState());
        waitUntilTerminated(isc.instanceId());
    }

    private void pollScalingActivity(String activityId) {
        while (true) {
            var activity = describeScalingActivity(activityId);
            var statusCode = activity.statusCode();
            log.info("{} ({})", activity.description(), statusCode);

            switch (statusCode) {
                case CANCELLED:
                    throw new AwsException("Scaling activity cancelled: " + activityId);
                case FAILED:
                    throw new AwsException("Scaling activity failed: " + activityId);
                case SUCCESSFUL:
                    log.info("Scaling activity successful: {}", activityId);
                    return;
                default:
                    sleep();
                    continue;
            }
        }
    }

    private Activity detachInstanceFromAsg(String instanceId, String asgName) {
        var request = DetachInstancesRequest.builder()
            .instanceIds(instanceId)
            .autoScalingGroupName(asgName)
            .shouldDecrementDesiredCapacity(Boolean.FALSE)
            .build();
        var response = autoScaling.detachInstances(request);
        return response.activities().get(0);
    }

    private Activity describeScalingActivity(String activityId) {
        var request = DescribeScalingActivitiesRequest.builder()
            .activityIds(activityId)
            .build();
        var response = autoScaling.describeScalingActivities(request);
        return response.activities().get(0);
    }

    private void waitUntilTerminated(String instanceId) {
        loop: while (true) {
            var request = DescribeInstanceStatusRequest.builder()
                .instanceIds(instanceId)
                .includeAllInstances(Boolean.TRUE)
                .build();
            var response = ec2.describeInstanceStatus(request);
            var status = response.instanceStatuses().get(0);
            var isc = status.instanceState().name();
            log.info("Instance {} is {}", instanceId, isc);
            switch (isc) {
                case TERMINATED:
                    break loop;
                case SHUTTING_DOWN:
                    sleep();
                    continue;
                default:
                    log.error("Unexpected instance state: {}", isc);
                    return;
            }
        }
    }
}
