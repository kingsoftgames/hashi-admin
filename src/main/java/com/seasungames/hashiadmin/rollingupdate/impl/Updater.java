package com.seasungames.hashiadmin.rollingupdate.impl;

import com.seasungames.hashiadmin.aws.Aws;
import com.seasungames.hashiadmin.consul.Consul;
import com.seasungames.hashiadmin.rollingupdate.Context;
import com.seasungames.hashiadmin.rollingupdate.Target;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.autoscaling.model.Instance;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.seasungames.hashiadmin.Utils.sleep;
import static java.util.stream.Collectors.toSet;

/**
 * Created by wangzhiguang on 2019-11-12.
 */
@Slf4j
public final class Updater {

    private final Target target;
    private final String asgName;
    private final Aws aws;
    private final Consul consul;

    private final Set<String> oldInstanceIds = new HashSet<>();

    public Updater(Target target, Context context) {
        target.init(context);
        this.target = target;
        this.asgName = context.autoScalingGroupName();
        this.aws = context.aws();
        this.consul = context.consul();
    }

    public void run() {
        var instances = listAsgInstances(asgName);
        printRollingUpdateInstances(instances);
        confirmRollingUpdate();
        doRollingUpdate(instances);
    }

    private void doRollingUpdate(List<Instance> instances) {

        final int total = instances.size();
        for (int i = 0; i < total; i++) {
            var oldInstance = instances.get(i);
            log.info("Updating instance {} ({}/{})", oldInstance.instanceId(), i + 1, total);
            var newInstance = detachOldInstance(oldInstance);
            prepareNewInstance(newInstance);
            retireOldInstance(oldInstance);
            terminateOldInstance(oldInstance);
            log.info("Updated instance {} ({}/{})", oldInstance.instanceId(), i + 1, total);
        }
    }

    private List<Instance> listAsgInstances(String asgName) {
        var instances = aws.listAsgInstances(asgName);
        printAsgInstances(instances);
        validateAsgInstances(instances);
        target.sortInstances(instances);
        return instances;
    }

    private void printAsgInstances(List<Instance> instances) {
        final int total = instances.size();
        log.info("Found {} instances in asg {}", total, asgName);
        for (int i = 0; i < total; i++) {
            var instance = instances.get(i);
            log.info("  ({}/{}) {}", i + 1, total, instance);
        }
    }

    private static void printRollingUpdateInstances(List<Instance> instances) {
        final int total = instances.size();
        log.info("Will do rolling update of {} instances in following order:", total);
        for (int i = 0; i < total; i++) {
            var instance = instances.get(i);
            log.info("  ({}/{}) {}", i + 1, total, instance);
        }
    }

    private static void confirmRollingUpdate() {
        final String prompt = "\nDo you want to perform these actions?\n  Only '%s' will be accepted to approve.\n\n  Enter a value: ";
        final String expected = "yes";
        String input = System.console().readLine(prompt, expected);
        if (expected.equals(input)) {
            System.out.println("\nAction approved.");
        } else {
            System.out.println("\nAction cancelled.");
            System.exit(1);
        }
    }

    private void validateAsgInstances(List<Instance> instances) {
        if (!instances.stream().allMatch(x -> x.launchConfigurationName() == null)) {
            final String message = "Some instance in asg has latest LaunchConfiguration, forgot to update asg with Terraform?";
            throw new RuntimeException(message);
        }
        var instanceIds = instances.stream().map(x -> x.instanceId()).collect(toSet());
        this.oldInstanceIds.addAll(instanceIds);
    }

    private Instance detachOldInstance(Instance oldInstance) {
        log.info("Detaching instance {} from asg {}", oldInstance.instanceId(), asgName);
        aws.detachInstance(oldInstance.instanceId(), asgName);
        log.info("Detached instance {} from asg {}", oldInstance.instanceId(), asgName);
        return waitForNewInstance();
    }

    private Instance waitForNewInstance() {
        log.info("Waiting for new instance to launch");
        Instance result;
        while (true) {
            var instances = aws.listAsgInstances(asgName);
            var newInstance = instances.stream()
                .filter(x -> !oldInstanceIds.contains(x.instanceId()))
                .findAny();
            if (newInstance.isPresent()) {
                result = newInstance.get();
                break;
            }
            sleep();
        }
        log.info("New instance launched: {}", result.instanceId());
        return result;
    }

    private void prepareNewInstance(Instance instance) {
        log.info("Preparing new instance: {}", instance.instanceId());
        target.prepareNewInstance(instance);
        oldInstanceIds.add(instance.instanceId());
        log.info("Prepared new instance: {}", instance.instanceId());
    }

    private void retireOldInstance(Instance instance) {
        log.info("Retiring old instance: {}", instance.instanceId());
        target.retireOldInstance(instance);
        log.info("Retired old instance: {}", instance.instanceId());
    }

    private void terminateOldInstance(Instance instance) {
        log.info("Terminating old instance: {}", instance.instanceId());
        aws.terminateInstance(instance.instanceId());
        log.info("Terminated old instance: {}", instance.instanceId());
    }
}
