package com.seasungames.hashiadmin.consul;

import com.seasungames.hashiadmin.consul.impl.ConsulImpl;

import java.util.List;

/**
 * Created by wangzhiguang on 2019-11-05.
 */
public interface Consul {

    static Consul create() {
        return new ConsulImpl();
    }

    List<ConsulServer> listServers();

    long getRaftLastLogIndex(ConsulServer server);

    void leave(String nodeIpAddress);
}
