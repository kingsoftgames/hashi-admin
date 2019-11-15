package com.seasungames.hashiadmin.consul.impl;

import com.alibaba.fastjson.JSONObject;
import com.seasungames.hashiadmin.consul.Consul;
import com.seasungames.hashiadmin.consul.ConsulHttpException;
import com.seasungames.hashiadmin.consul.ConsulServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.seasungames.hashiadmin.Utils.getenv;
import static com.seasungames.hashiadmin.Utils.normalizeHttpAddr;

/**
 * Created by wangzhiguang on 2019-11-13.
 */
public final class ConsulImpl implements Consul {

    private static final String DEFAULT_CONSUL_ADDR = "http://127.0.0.1:8500";

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(1);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(3);

    private final String consulHttpAddr;
    private final HttpClient httpClient;

    public ConsulImpl() {
        String addr = getenv("CONSUL_HTTP_ADDR", DEFAULT_CONSUL_ADDR);
        this.consulHttpAddr = normalizeHttpAddr(addr);
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .build();
    }

    @Override
    public List<ConsulServer> listServers() {
        var body = doGet(this.consulHttpAddr, "/v1/operator/raft/configuration");
        return parseConsulServers(body);
    }

    @Override
    public long getRaftLastLogIndex(String serverIpAddress) {
        var httpAddr = String.format("http://%s:8500", serverIpAddress);
        var body = doGet(httpAddr, "/v1/agent/self");
        return parseRaftLastLogIndex(body);
    }

    private JSONObject doGet(String consulAddr, String path) {
        var request = createHttpRequest(consulAddr, "GET", path, BodyPublishers.noBody());
        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new ConsulHttpException(path, e);
        }
        if (response.statusCode() == 200) {
            return JSONObject.parseObject(response.body());
        } else {
            throw new ConsulHttpException(path, response.statusCode(), response.body());
        }
    }

    private static HttpRequest createHttpRequest(String consulAddr, String method, String path, BodyPublisher bodyPublisher) {
        return HttpRequest.newBuilder()
            .uri(URI.create(consulAddr + path))
            .method(method, bodyPublisher)
            .timeout(REQUEST_TIMEOUT)
            .build();
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
