package com.seasungames.hashiadmin.consul;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Created by wangzhiguang on 2019-11-05.
 */
@Getter
@Setter
@Accessors(fluent = true)
public final class ConsulServer {
    private String id;
    private String node;
    private String ipAddress;
    private boolean leader;
    private boolean voter;
}
