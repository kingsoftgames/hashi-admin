package com.seasungames.hashiadmin;

import software.amazon.awssdk.services.autoscaling.model.Instance;

import java.util.Comparator;

/**
 * Created by wangzhiguang on 2019-11-04.
 */
public final class Utils {

    private static final long SLEEP_INTERVAL_MS = 5000L;

    public static String getenv(String name, String def) {
        String value = System.getenv(name);
        return value != null ? value : def;
    }

    public static void sleep() {
        try {
            Thread.sleep(SLEEP_INTERVAL_MS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String normalizeHttpAddr(String addr) {
        return addr.endsWith("/") ? addr.substring(0, addr.length() - 1) : addr;
    }

    // Returns a Comparator that moves Instance with the specified ID to last element in a List
    public static Comparator<Instance> moveInstanceToLast(String instanceId) {
        return ((o1, o2) -> {
            if (o1.instanceId().equals(instanceId))
                return 1;
            else if (o2.instanceId().equals(instanceId))
                return -1;
            else
                return 0;
        });
    }
}
