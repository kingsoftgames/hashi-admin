package com.seasungames.hashiadmin.rollingupdate.impl;

import com.seasungames.hashiadmin.consul.ConsulTarget;
import com.seasungames.hashiadmin.nomad.NomadTarget;
import com.seasungames.hashiadmin.rollingupdate.Target;

import java.util.function.Supplier;

/**
 * Created by wangzhiguang on 2019-11-12.
 */
public enum TargetName implements Supplier<Target> {

    consul(ConsulTarget::new),

    nomad(NomadTarget::new);

    @Override
    public Target get() {
        return this.supplier.get();
    }

    TargetName(Supplier<Target> supplier) {
        this.supplier = supplier;
    }

    private final Supplier<Target> supplier;
}
