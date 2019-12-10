package com.seasungames.hashiadmin.nomad;

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
public class NomadTarget extends AbstractTarget {

    private Nomad nomad;

    @Override
    public void init(Context context) {
        super.init(context);
        this.nomad = context.nomad();
    }

    @Override
    public void sortInstances(List<Instance> instances) {
        var nomadLeader = getNomadLeader();
        var instanceId = nomadLeader.name();
        Collections.sort(instances, moveInstanceToLast(instanceId));
    }

    @Override
    public void prepareNewInstance(Instance newInstance) {
        var instanceId = newInstance.instanceId();
        waitForNewNode(instanceId);
        waitForRaftLogReplication(instanceId);
    }

    @Override
    public void retireOldInstance(Instance oldInstance) {
        // Nothing to do
    }

    private NomadServer getNomadLeader() {
        var servers = nomad.listServers();
        return servers.stream()
            .filter(x -> x.leader())
            .findFirst()
            .orElseThrow();
    }

    private void waitForNewNode(String instanceId) {
        log.info("Waiting for new Nomad node to appear: {}", instanceId);
        while (true) {
            var servers = nomad.listServers();
            var newNode = servers.stream()
                .filter(x -> x.name().equals(instanceId))
                .findFirst();
            if (newNode.isPresent()) {
                log.info("New Nomad node appeared: {}", instanceId);
                return;
            } else {
                sleep();
            }
        }
    }

    private void waitForRaftLogReplication(String instanceId) {
        log.info("Waiting for raft log replication to node: {}", instanceId);
        while (true) {
            var servers = nomad.listServers();
            var leader = getServerLeader(servers);
            var follower = getServerByName(servers, instanceId);
            log.info("leader={}, raftLastIndex={}, follower={}, raftLastIndex={}",
                leader.ipAddress(), leader.raftLastIndex(),
                follower.ipAddress(), follower.raftLastIndex());
            if (leader.raftLastIndex() == follower.raftLastIndex()) {
                log.info("Raft logs have fully replicated to node {}", instanceId);
                return;
            } else {
                sleep();
            }
        }
    }

    private static NomadServer getServerLeader(List<NomadServer> servers) {
        return servers.stream()
            .filter(x -> x.leader())
            .findFirst()
            .orElseThrow();
    }

    private static NomadServer getServerByName(List<NomadServer> servers, String name) {
        return servers.stream()
            .filter(x -> x.name().equals(name))
            .findFirst()
            .orElseThrow();
    }
}
