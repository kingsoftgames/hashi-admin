package com.seasungames.hashiadmin.consul;

import com.seasungames.hashiadmin.rollingupdate.AbstractTarget;
import com.seasungames.hashiadmin.rollingupdate.Context;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.services.autoscaling.model.Instance;

import java.util.Collections;
import java.util.List;

import static com.seasungames.hashiadmin.Utils.moveInstanceToLast;
import static com.seasungames.hashiadmin.Utils.sleep;

/**
 * Created by wangzhiguang on 2019-11-12.
 */
@Log4j2
public final class ConsulTarget extends AbstractTarget {

    private Consul consul;

    @Override
    public void init(Context context) {
        super.init(context);
        this.consul = context.consul();
    }

    @Override
    public void sortInstances(List<Instance> instances) {
        var consulLeader = getConsulLeader();
        var instanceId = consulLeader.node();
        Collections.sort(instances, moveInstanceToLast(instanceId));
    }

    @Override
    public void prepareNewInstance(Instance newInstance) {
        var node = waitForNewNode(newInstance.instanceId());
        waitForRaftLogReplication(node);
    }

    @Override
    public void retireOldInstance(Instance oldInstance) {
        // Nothing to do
    }

    private ConsulServer getConsulLeader() {
        var servers = consul.listServers();
        return servers.stream()
            .filter(x -> x.leader())
            .findFirst()
            .orElseThrow();
    }

    private ConsulServer getConsulServerByNodeId(String nodeId) {
        var servers = consul.listServers();
        return servers.stream()
            .filter(x -> x.node().equals(nodeId))
            .findFirst()
            .orElseThrow();
    }

    private ConsulServer waitForNewNode(String newNodeId) {
        log.info("Waiting for new Consul node to appear: {}", newNodeId);
        while (true) {
            var servers = consul.listServers();
            var newNode = servers.stream()
                .filter(x -> x.node().equals(newNodeId))
                .findFirst();
            if (newNode.isPresent()) {
                log.info("New Consul node appeared: {}", newNodeId);
                return newNode.get();
            } else {
                sleep();
            }
        }
    }

    // Wait for raft.last_log_index of new node converge to the same value of consul leader.
    // https://www.consul.io/docs/install/bootstrapping.html#verifying-the-cluster-and-connect-the-clients
    // https://learn.hashicorp.com/consul/day-2-operations/servers#server-coordination
    private void waitForRaftLogReplication(ConsulServer follower) {
        log.info("Waiting for raft log replication to node: {}", follower.node());
        while (true) {
            var leader = getConsulLeader();
            var leaderRaftLastLogIndex = consul.getRaftLastLogIndex(leader);
            var followerRaftLastLogIndex = consul.getRaftLastLogIndex(follower);
            log.info("leader={}, raft.last_log_index={}, follower={}, raft.last_log_index={}",
                leader.ipAddress(), leaderRaftLastLogIndex,
                follower.ipAddress(), followerRaftLastLogIndex);
            if (leaderRaftLastLogIndex == followerRaftLastLogIndex) {
                log.info("raft.last_log_index of leader and follower has converged to the same value.");
                return;
            } else {
                sleep();
            }
        }
    }
}
