package com.seasungames.hashiadmin.consul.impl;

import com.alibaba.fastjson.JSONObject;
import com.seasungames.hashiadmin.consul.Consul;
import com.seasungames.hashiadmin.consul.ConsulServer;
import com.seasungames.hashiadmin.http.HttpApi;

import java.net.URI;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.ArrayList;
import java.util.List;

import static com.seasungames.hashiadmin.Utils.getenv;
import static com.seasungames.hashiadmin.Utils.normalizeHttpAddr;

/**
 * Created by wangzhiguang on 2019-11-13.
 */
public final class ConsulImpl extends HttpApi implements Consul {

    private static final String DEFAULT_CONSUL_ADDR = "http://127.0.0.1:8500";

    private final String consulHttpAddr;

    public ConsulImpl() {
        String addr = getenv("CONSUL_HTTP_ADDR", DEFAULT_CONSUL_ADDR);
        this.consulHttpAddr = normalizeHttpAddr(addr);
    }

    @Override
    public List<ConsulServer> listServers() {
        var body = getJson("/v1/operator/raft/configuration");
        return parseConsulServers(body);
    }

    @Override
    public long getRaftLastLogIndex(ConsulServer server) {
        var httpAddr = getConsulHttpAddr(server.ipAddress());
        var body = getJson(httpAddr, "/v1/agent/self");
        return parseRaftLastLogIndex(body);
    }

    @Override
    public void leave(String nodeIpAddress) {
        var httpAddr = getConsulHttpAddr(nodeIpAddress);
        var uri = URI.create(httpAddr + "/v1/agent/leave");
        putJson(uri, BodyPublishers.noBody());
    }

    private static String getConsulHttpAddr(String nodeIpAddress) {
        return String.format("http://%s:8500", nodeIpAddress);
    }

    private JSONObject getJson(String path) {
        return getJson(this.consulHttpAddr, path);
    }

    private JSONObject getJson(String consulHttpAddr, String path) {
        var uri = URI.create(consulHttpAddr + path);
        return getJson(uri);
    }

    private static List<ConsulServer> parseConsulServers(JSONObject body) {
        var array = body.getJSONArray("Servers");
        var size = array.size();
        List<ConsulServer> servers = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            var o = array.getJSONObject(i);
            var s = parseConsulServer(o);
            servers.add(s);
        }
        return servers;
    }

    private static ConsulServer parseConsulServer(JSONObject json) {
        return new ConsulServer()
            .id(json.getString("ID"))
            .node(json.getString("Node"))
            .ipAddress(json.getString("Address").split(":")[0])
            .leader(json.getBooleanValue("Leader"))
            .voter(json.getBooleanValue("Voter"));
    }

    private static long parseRaftLastLogIndex(JSONObject body) {
        var stats = body.getJSONObject("Stats");
        var raft = stats.getJSONObject("raft");
        return raft.getLongValue("last_log_index");
    }
}
