package com.seasungames.hashiadmin.nomad;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(fluent = true)
@ToString
public final class NomadClient {

    private String id;          // f7f1a5d4-3186-8def-d7cb-97e266bef2de
    private String name;        // i-0152e202557c6979d (EC2 Instance ID)
    private String datacenter;  // cn-north-1a
    private String ipAddress;   // 10.0.0.207
    private String nodeClass;   // backend
    private String version;     // 0.10.3
    private String status;      // initializing/ready/down
    private boolean eligible;
    private boolean drain;
}
