package com.seasungames.hashiadmin.http;

import java.net.URI;

/**
 * Created by wangzhiguang on 2019-11-05.
 */
public final class HttpException extends RuntimeException {

    private final URI uri;
    private final int statusCode;
    private final String body;

    public HttpException(URI uri, int statusCode, String body) {
        super(String.format("%s, HTTP %d, %s", uri, statusCode, body));
        this.uri = uri;
        this.statusCode = statusCode;
        this.body = body;
    }

    public HttpException(URI uri, Throwable cause) {
        super(uri.toString(), cause);
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
