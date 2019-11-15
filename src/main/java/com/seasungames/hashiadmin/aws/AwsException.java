package com.seasungames.hashiadmin.aws;

/**
 * Created by wangzhiguang on 2019-11-13.
 */
public class AwsException extends RuntimeException {

    public AwsException(String message) {
        super(message);
    }

    public AwsException(String message, Throwable cause) {
        super(message, cause);
    }

    public AwsException(Throwable cause) {
        super(cause);
    }
}
