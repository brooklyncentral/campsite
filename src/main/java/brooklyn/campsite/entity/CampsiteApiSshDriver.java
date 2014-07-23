package brooklyn.campsite.entity;

import java.util.Map;

import com.google.common.net.HostAndPort;

import brooklyn.entity.basic.Attributes;
import brooklyn.entity.webapp.nodejs.NodeJsWebAppService;
import brooklyn.entity.webapp.nodejs.NodeJsWebAppSshDriver;
import brooklyn.location.basic.SshMachineLocation;
import brooklyn.util.ResourceUtils;
import brooklyn.util.collections.MutableMap;
import brooklyn.util.os.Os;
import brooklyn.util.ssh.BashCommands;
import brooklyn.util.stream.Streams;
import brooklyn.util.text.Strings;
import brooklyn.util.text.TemplateProcessor;

public class CampsiteApiSshDriver extends NodeJsWebAppSshDriver implements CampsiteApiDriver {

    public CampsiteApiSshDriver(CampsiteApiImpl entity, SshMachineLocation machine) {
        super(entity, machine);
    }

    @Override
    public void customize() {
        super.customize();

        Map<String,Object> substitutions = MutableMap.<String,Object>builder()
                .put("databaseUser", getEntity().getConfig(CampsiteApi.DATABASE_USER))
                .put("databasePassword", getEntity().getConfig(CampsiteApi.DATABASE_PASSWORD))
                .build();

        String databaseHostAndPort = getEntity().getConfig(CampsiteApi.DATABASE_HOST_AND_PORT);
        if (Strings.isNonBlank(databaseHostAndPort)) {
            HostAndPort endpoint = HostAndPort.fromString(databaseHostAndPort);
            substitutions.put("databaseHost", endpoint.getHostText());
            substitutions.put("databasePort", endpoint.getPort());
        } else {
            substitutions.put("databaseHost", getEntity().getConfig(CampsiteApi.DATABASE_HOST));
            substitutions.put("databasePort", getEntity().getConfig(CampsiteApi.DATABASE_PORT));
        }

        String template = ResourceUtils.create().getResourceAsString(getEntity().getConfig(CampsiteApi.CONFIG_JSON_TEMPLATE_URL));
        String contents = TemplateProcessor.processTemplateContents(template, substitutions);
        getMachine().copyTo(Streams.newInputStreamWithContents(contents), Os.mergePaths(getRunDir(), "node-api", "config_data.json"));

        newScript(CUSTOMIZING)
                .updateTaskAndFailOnNonZeroResultCode()
                .body.append(
                        BashCommands.sudo(String.format("sed -i.bak -e 's/var basePort = [0-9]*;/var basePort = %d;/g' %s",
                                getEntity().getAttribute(Attributes.HTTP_PORT),
                                Os.mergePaths(getRunDir(), "node-api", "common.js"))))
                .execute();
    }

    @Override
    public boolean isRunning() {
        return newScript(MutableMap.of(USE_PID_FILE, false), CHECK_RUNNING)
                .body.append(
                        BashCommands.sudo("forever list > /tmp/list.txt"),
                        BashCommands.sudo("forever list | grep --quiet " + getEntity().getConfig(NodeJsWebAppService.APP_FILE)))
                .execute() == 0;
    }

    @Override
    public void stop() {
        newScript(MutableMap.of(USE_PID_FILE, false), STOPPING)
                .body.append(BashCommands.sudo("forever stop " + getEntity().getConfig(NodeJsWebAppService.APP_FILE)))
                .execute();
    }

}
