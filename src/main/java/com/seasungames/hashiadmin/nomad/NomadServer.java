package com.seasungames.hashiadmin.nomad;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Created by wangzhiguang on 2019-12-06.
 */
@Getter
@Setter
@Accessors(fluent = true)
@ToString
public final class NomadServer {

    // The following fields are populated with response from /v1/operator/autopilot/health
    // https://www.nomadproject.io/api/operator.html#read-health

    private String id;          // 621c3d09-7edc-779f-7c66-9c1c5ce05ad8
    private String name;        // i-0ba7e0a18338c24bc (EC2 Instance ID)
    private String region;      // cn-northwest-1
    private String ipAddress;   // 10.1.1.167
    private String version;     // 0.10.2
    private long raftLastIndex; // 238957
    private boolean leader;
    private boolean voter;
    private boolean healthy;
}
