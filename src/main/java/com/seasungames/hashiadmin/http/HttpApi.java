package com.seasungames.hashiadmin.http;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * Created by wangzhiguang on 2019-12-06.
 */
public abstract class HttpApi {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(1);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(3);

    private final HttpClient httpClient;

    protected HttpApi() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .build();
    }

    protected JSONObject getJson(URI uri) {
        return getJson(uri, emptyMap());
    }

    protected JSONObject getJson(URI uri, Map<String, String> headers) {
        var request = createHttpRequest(uri, "GET", headers, BodyPublishers.noBody());
        return requestJson(request);
    }

    protected JSONObject putJson(URI uri, HttpRequest.BodyPublisher body) {
        return putJson(uri, emptyMap(), body);
    }

    protected JSONObject putJson(URI uri, Map<String, String> headers, HttpRequest.BodyPublisher body) {
        var request = createHttpRequest(uri, "PUT", headers, BodyPublishers.noBody());
        return requestJson(request);
    }

    private static HttpRequest createHttpRequest(URI uri, String method, Map<String, String> headers, BodyPublisher bodyPublisher) {
        var builder = HttpRequest.newBuilder()
            .uri(uri)
            .method(method, bodyPublisher)
            .timeout(REQUEST_TIMEOUT);
        headers.forEach((k, v) -> builder.setHeader(k, v));
        return builder.build();
    }

    private JSONObject requestJson(HttpRequest request) {
        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new HttpException(request.uri(), e);
        }
        if (response.statusCode() == 200) {
            return JSONObject.parseObject(response.body());
        } else {
            throw new HttpException(request.uri(), response.statusCode(), response.body());
        }
    }
}
