package com.seasungames.hashiadmin.consul;

import java.net.URI;

/**
 * Created by wangzhiguang on 2019-11-05.
 */
public final class ConsulHttpException extends RuntimeException {

    private final URI uri;
    private final int statusCode;
    private final String body;

    public ConsulHttpException(URI uri, int statusCode, String body) {
        this.uri = uri;
        this.statusCode = statusCode;
        this.body = body;
    }

    public ConsulHttpException(URI uri, Throwable cause) {
        super(cause);
        this.uri = uri;
        this.statusCode = 0;
        this.body = "";
    }

    public URI getUri() {
        return uri;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }
}
