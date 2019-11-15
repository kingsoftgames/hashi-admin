package com.seasungames.hashiadmin.rollingupdate;

import com.seasungames.hashiadmin.rollingupdate.impl.ContextImpl;
import com.seasungames.hashiadmin.rollingupdate.impl.TargetName;
import com.seasungames.hashiadmin.rollingupdate.impl.Updater;
import lombok.extern.log4j.Log4j2;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import software.amazon.awssdk.regions.Region;

import java.util.concurrent.Callable;

/**
 * Created by wangzhiguang on 2019-11-12.
 */

@Log4j2
@Command(name = "rolling-update", mixinStandardHelpOptions = true,
    description = "Run a rolling update of an AWS Auto Scaling Group.")
public final class RollingUpdate implements Callable<Integer> {

    @Parameters(index = "0",
        paramLabel = "<auto-scaling-group>",
        description = "The name of the Auto Scaling Group to run the rolling update.")
    private String asgName;

    @Option(names = "--target", required = true, description = "The target to run rolling update: ${COMPLETION-CANDIDATES}")
    private TargetName targetName;

    @Option(names = "--region", required = true, description = "The AWS region to use.")
    private String awsRegion;

    @Override
    public Integer call() throws Exception {
        log.info("Start rolling update of a {} asg {}, in region {}", targetName, asgName, awsRegion);
        var target = this.targetName.get();
        var context = new ContextImpl(this.asgName, Region.of(this.awsRegion));
        var updater = new Updater(target, context);

        try {
            updater.run();
        } catch (Throwable t) {
            log.error("Unhandled exception", t);
            log.error("Finished: FAILURE");
            return 1;
        }

        log.info("Finished: SUCCESS");
        return 0;
    }
}
