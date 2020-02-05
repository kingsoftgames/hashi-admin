package com.seasungames.hashiadmin.nomad;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(fluent = true)
public final class NomadClientList {

    private long index;
    private List<NomadClient> clients;
}
