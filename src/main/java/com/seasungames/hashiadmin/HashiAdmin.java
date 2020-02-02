package com.seasungames.hashiadmin;

import com.seasungames.hashiadmin.rollingupdate.RollingUpdate;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

@Command(name = "hashi-admin", mixinStandardHelpOptions = true,
    description = "A sysadmin tool for Hashicorp products (Consul, Nomad).",
    subcommands = {
        RollingUpdate.class
    })
public class HashiAdmin implements Runnable {

    static {
        initLogging();
    }

    private static void initLogging() {
        try (InputStream is = HashiAdmin.class.getResourceAsStream("/logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
