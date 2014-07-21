/*
 * Copyright 2014 by Cloudsoft Corporation Limited
 */
package brooklyn.campsite;

import io.airlift.command.Command;
import io.airlift.command.Option;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import brooklyn.catalog.BrooklynCatalog;
import brooklyn.cli.Main;

import com.google.common.base.Objects.ToStringHelper;

/**
 * Launch Brooklyn.
 */
public class CampsiteMain extends Main {

    private static final Logger log = LoggerFactory.getLogger(CampsiteMain.class);

    public static void main(String...argv) {
        log.debug("CLI invoked with args "+ Arrays.asList(argv));
        new CampsiteMain().execCli(argv);
    }

    @Override
    protected String cliScriptName() {
        return "start.sh";
    }

    @Override
    protected Class<? extends BrooklynCommand> cliLaunchCommand() {
        return LaunchCommand.class;
    }

    @Command(name = "launch", description = "Starts Brooklyn, and optionally an application. " +
            "Use --campsite to launch Campsite.")
    public static class LaunchCommand extends Main.LaunchCommand {

        @Option(name = { "--campsite" }, description = "Launch Campsite")
        public boolean campsite;

        @Override
        public Void call() throws Exception {
            // process our CLI arguments
            if (campsite) setAppToLaunch(CampsiteApplication.class.getCanonicalName() );

            // now process the standard launch arguments
            return super.call();
        }

        @Override
        protected void populateCatalog(BrooklynCatalog catalog) {
            super.populateCatalog(catalog);
            catalog.addItem(CampsiteApplication.class);
        }

        @Override
        public ToStringHelper string() {
            return super.string().add("campsite", campsite);
        }
    }
}
