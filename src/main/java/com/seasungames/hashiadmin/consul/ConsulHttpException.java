package com.seasungames.hashiadmin.consul;

/**
 * Created by wangzhiguang on 2019-11-05.
 */
public final class ConsulHttpException extends RuntimeException {

    private final String path;
    private final int statusCode;
    private final String body;

    public ConsulHttpException(String path, int statusCode, String body) {
        this.path = path;
        this.statusCode = statusCode;
        this.body = body;
    }

    public ConsulHttpException(String path, Throwable cause) {
        super(cause);
        this.path = path;
        this.statusCode = 0;
        this.body = "";
    }

    public String getPath() {
        return path;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }
}
