package com.seasungames.hashiadmin.nomad;

import com.seasungames.hashiadmin.nomad.impl.NomadImpl;

import java.util.List;

/**
 * Created by wangzhiguang on 2019-12-05.
 */
public interface Nomad {

    static Nomad create() {
        return new NomadImpl();
    }

    List<NomadServer> listServers();

    NomadClientList listClients(long index);
}
