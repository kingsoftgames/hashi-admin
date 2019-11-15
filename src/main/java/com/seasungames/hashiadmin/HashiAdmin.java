package com.seasungames.hashiadmin;

import com.seasungames.hashiadmin.rollingupdate.RollingUpdate;
import lombok.extern.log4j.Log4j2;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

/**
 * Hello world!
 */
@Log4j2
@Command(name = "hashi-admin", mixinStandardHelpOptions = true,
    description = "A sysadmin tool for Hashicorp products (Consul, Nomad).",
    subcommands = {
        RollingUpdate.class
    })
public class HashiAdmin implements Runnable {

    @Spec
    private CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().usage(System.err);
    }

    public static void main(String[] args) {
        int status = new CommandLine(new HashiAdmin()).execute(args);
        System.exit(status);
    }
}
