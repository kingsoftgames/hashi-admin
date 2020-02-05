package com.seasungames.hashiadmin.nomad.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.seasungames.hashiadmin.http.HttpApi;
import com.seasungames.hashiadmin.http.HttpException;
import com.seasungames.hashiadmin.nomad.Nomad;
import com.seasungames.hashiadmin.nomad.NomadClient;
import com.seasungames.hashiadmin.nomad.NomadClientList;
import com.seasungames.hashiadmin.nomad.NomadServer;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.seasungames.hashiadmin.Utils.getenv;
import static com.seasungames.hashiadmin.Utils.normalizeHttpAddr;
import static java.util.Collections.emptyMap;

/**
 * Created by wangzhiguang on 2019-12-06.
 */
public final class NomadImpl extends HttpApi implements Nomad {

    private static final String DEFAULT_NOMAD_ADDR = "http://127.0.0.1:4646";

    private final String nomadAddr;
    private final Optional<String> nomadToken;

    public NomadImpl() {
        this.nomadAddr = getNomadAddr();
        this.nomadToken = getNomadToken();
    }

    @Override
    public List<NomadServer> listServers() {
        var body = queryAutopilotHealth();
        return parseNomadServers(body);
    }

    @Override
    public NomadClientList listClients(long index) {
        return listNodes(index);
    }

    private static String getNomadAddr() {
        String addr = getenv("NOMAD_ADDR", DEFAULT_NOMAD_ADDR);
        return normalizeHttpAddr(addr);
    }

    private static Optional<String> getNomadToken() {
        String token = getenv("NOMAD_TOKEN", null);
        return Optional.ofNullable(token);
    }

    private static long getBlockingQueryIndex(HttpResponse<String> response) {
        return response.headers()
            .firstValue("X-Nomad-Index")
            .map(Long::parseLong)
            .orElseThrow();
    }

    private JSONObject queryAutopilotHealth() {
        try {
            return getJson("/v1/operator/autopilot/health");
        } catch (HttpException e) {
            // HTTP 429 is OK, because the request may hit the new node
            // https://www.nomadproject.io/api/operator.html#read-health
            if (e.getStatusCode() == 429) {
                return JSONObject.parseObject(e.getBody());
            } else {
                throw e;
            }
        }
    }

    private NomadClientList listNodes(long index) {
        var response = doGet("/v1/nodes", index);
        var body = parseJsonArray(response);
        var clients = parseNomadClients(body);
        var newIndex = getBlockingQueryIndex(response);
        return new NomadClientList()
            .index(newIndex)
            .clients(clients);
    }

    private JSONObject getJson(String path) {
        return getJson(this.nomadAddr, path);
    }

    private JSONObject getJson(String nomadAddr, String path) {
        var uri = URI.create(nomadAddr + path);
        var headers = httpHeaders();
        return getJson(uri, headers);
    }

    private HttpResponse<String> doGet(String path, long index) {
        String str = String.format("%s%s?index=%d", this.nomadAddr, path, index);
        var uri = URI.create(str);
        var headers = httpHeaders();
        return doGet(uri, headers);
    }

    private Map<String, String> httpHeaders() {
        if (nomadToken.isPresent()) {
            return Map.of("X-Nomad-Token", nomadToken.get());
        } else {
            return emptyMap();
        }
    }

    private static List<NomadServer> parseNomadServers(JSONObject body) {
        var array = body.getJSONArray("Servers");
        var size = array.size();
        List<NomadServer> servers = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            var o = array.getJSONObject(i);
            var s = parseNomadServer(o);
            servers.add(s);
        }
        return servers;
    }

    // https://www.nomadproject.io/api/operator.html#read-health
    private static NomadServer parseNomadServer(JSONObject json) {
        return new NomadServer()
            .id(json.getString("ID"))
            .name(json.getString("Name").split("\\.")[0])
            .region(json.getString("Name").split("\\.")[1])
            .ipAddress(json.getString("Address").split(":")[0])
            .version(json.getString("Version"))
            .raftLastIndex(json.getLongValue("LastIndex"))
            .leader(json.getBooleanValue("Leader"))
            .voter(json.getBooleanValue("Voter"))
            .healthy(json.getBooleanValue("Healthy"));
    }

    private static List<NomadClient> parseNomadClients(JSONArray array) {
        var size = array.size();
        List<NomadClient> clients = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            var o = array.getJSONObject(i);
            var s = parseNomadClient(o);
            clients.add(s);
        }
        return clients;
    }

    // https://www.nomadproject.io/api/nodes.html#list-nodes
    private static NomadClient parseNomadClient(JSONObject json) {
        return new NomadClient()
            .id(json.getString("ID"))
            .name(json.getString("Name"))
            .datacenter(json.getString("Datacenter"))
            .ipAddress(json.getString("Address"))
            .nodeClass(json.getString("NodeClass"))
            .version(json.getString("Version"))
            .status(json.getString("Status"))
            .eligible("eligible".equals(json.getString("SchedulingEligibility")))
            .drain(json.getBooleanValue("Drain"));
    }
}
