package brooklyn.campsite.entity;

import java.util.Map;

import brooklyn.entity.webapp.nodejs.NodeJsWebAppService;
import brooklyn.entity.webapp.nodejs.NodeJsWebAppSshDriver;
import brooklyn.location.basic.SshMachineLocation;
import brooklyn.util.ResourceUtils;
import brooklyn.util.collections.MutableMap;
import brooklyn.util.os.Os;
import brooklyn.util.ssh.BashCommands;
import brooklyn.util.stream.Streams;
import brooklyn.util.text.TemplateProcessor;

public class CampsiteApiSshDriver extends NodeJsWebAppSshDriver implements CampsiteApiDriver {

    public CampsiteApiSshDriver(CampsiteApiImpl entity, SshMachineLocation machine) {
        super(entity, machine);
    }

    @Override
    public void customize() {
        super.customize();

        Map<String,Object> substitutions = MutableMap.<String,Object>builder()
                .put("databaseHost", getEntity().getConfig(CampsiteApi.DATABASE_HOST))
                .put("databasePort", getEntity().getConfig(CampsiteApi.DATABASE_PORT)) // TODO
                .put("databaseUser", getEntity().getConfig(CampsiteApi.DATABASE_USER))
                .put("databasePassword", getEntity().getConfig(CampsiteApi.DATABASE_PASSWORD))
                .build();

        String template = ResourceUtils.create().getResourceAsString(getEntity().getConfig(CampsiteApi.CONFIG_JSON_TEMPLATE_URL));
        String contents = TemplateProcessor.processTemplateContents(template, substitutions);
        getMachine().copyTo(Streams.newInputStreamWithContents(contents), Os.mergePaths(getRunDir(), "node-api", "config_data.json"));

        newScript(CUSTOMIZING)
                .updateTaskAndFailOnNonZeroResultCode()
                .body.append(
                        BashCommands.sudo(String.format("sed -i.bak -e 's/var basePort = [0-9]*;/var basePort = %d;/g' %s",
                                getEntity().getAttribute(CampsiteApi.API_PORT),
                                Os.mergePaths(getRunDir(), "node-api", "common.js"))))
                .execute();
    }

    @Override
    public boolean isRunning() {
        return newScript(MutableMap.of(USE_PID_FILE, false), CHECK_RUNNING)
                .body.append(BashCommands.sudo("forever list | grep --quiet " + getEntity().getConfig(NodeJsWebAppService.APP_FILE)))
                .execute() == 0;
    }

    @Override
    public void stop() {
        newScript(MutableMap.of(USE_PID_FILE, false), STOPPING)
                .body.append(BashCommands.sudo("forever stop " + getEntity().getConfig(NodeJsWebAppService.APP_FILE)))
                .execute();
    }

}
