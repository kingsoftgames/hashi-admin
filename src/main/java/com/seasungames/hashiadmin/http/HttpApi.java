package com.seasungames.hashiadmin.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
import java.util.function.Function;

import static java.util.Collections.emptyMap;

/**
 * Created by wangzhiguang on 2019-12-06.
 */
public abstract class HttpApi {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(1);

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
        var response = doGet(uri, headers);
        return parseJsonObject(response);
    }

    protected JSONObject postJson(URI uri, HttpRequest.BodyPublisher body) {
        return postJson(uri, emptyMap(), body);
    }

    protected JSONObject postJson(URI uri, Map<String, String> headers, HttpRequest.BodyPublisher body) {
        var response = doPost(uri, headers, body);
        return parseJsonObject(response);
    }

    protected JSONObject putJson(URI uri, HttpRequest.BodyPublisher body) {
        return putJson(uri, emptyMap(), body);
    }

    protected JSONObject putJson(URI uri, Map<String, String> headers, HttpRequest.BodyPublisher body) {
        var response = doPut(uri, headers, body);
        return parseJsonObject(response);
    }

    protected HttpResponse<String> doGet(URI uri) {
        return doGet(uri, emptyMap());
    }

    protected HttpResponse<String> doGet(URI uri, Map<String, String> headers) {
        return doHttpRequest(uri, "GET", headers, BodyPublishers.noBody());
    }

    protected HttpResponse<String> doPost(URI uri, HttpRequest.BodyPublisher body) {
        return doPost(uri, emptyMap(), body);
    }

    protected HttpResponse<String> doPost(URI uri, Map<String, String> headers, BodyPublisher body) {
        return doHttpRequest(uri, "POST", headers, BodyPublishers.noBody());
    }

    protected HttpResponse<String> doPut(URI uri, HttpRequest.BodyPublisher body) {
        return doPut(uri, emptyMap(), body);
    }

    protected HttpResponse<String> doPut(URI uri, Map<String, String> headers, BodyPublisher body) {
        return doHttpRequest(uri, "PUT", headers, body);
    }

    protected static JSONObject parseJsonObject(HttpResponse<String> response) {
        return parseBody(response, JSON::parseObject);
    }

    protected static JSONArray parseJsonArray(HttpResponse<String> response) {
        return parseBody(response, JSON::parseArray);
    }

    private static <R> R parseBody(HttpResponse<String> response, Function<String, R> parser) {
        if (response.statusCode() == 200) {
            return parser.apply(response.body());
        } else {
            throw new HttpException(response.uri(), response.statusCode(), response.body());
        }
    }

    private HttpResponse<String> doHttpRequest(URI uri, String method, Map<String, String> headers, BodyPublisher body) {
        var request = createHttpRequest(uri, method, headers, body);
        return sendHttpRequest(request);
    }

    private static HttpRequest createHttpRequest(URI uri, String method, Map<String, String> headers, BodyPublisher bodyPublisher) {
        var builder = HttpRequest.newBuilder()
            .uri(uri)
            .method(method, bodyPublisher);
        headers.forEach((k, v) -> builder.setHeader(k, v));
        return builder.build();
    }

    private HttpResponse<String> sendHttpRequest(HttpRequest request) {
        try {
            return httpClient.send(request, BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new HttpException(request.uri(), e);
        }
    }
}
