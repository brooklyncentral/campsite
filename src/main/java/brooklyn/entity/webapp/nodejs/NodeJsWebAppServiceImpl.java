package brooklyn.entity.webapp.nodejs;

import com.google.common.base.Predicates;
import com.google.common.net.HostAndPort;

import brooklyn.entity.basic.SoftwareProcessImpl;
import brooklyn.entity.webapp.WebAppServiceMethods;
import brooklyn.event.feed.http.HttpFeed;
import brooklyn.event.feed.http.HttpPollConfig;
import brooklyn.event.feed.http.HttpValueFunctions;
import brooklyn.location.access.BrooklynAccessUtils;

public class NodeJsWebAppServiceImpl extends SoftwareProcessImpl implements NodeJsWebAppService {

    private transient HttpFeed httpFeed;

    @Override
    protected void connectSensors() {
        super.connectSensors();

        HostAndPort accessible = BrooklynAccessUtils.getBrooklynAccessibleAddress(this, getAttribute(HTTP_PORT));
        String nodeJsUrl = String.format("http://%s:%d", accessible.getHostText(), accessible.getPort());
        httpFeed = HttpFeed.builder()
                .entity(this)
                .baseUri(nodeJsUrl)
                .poll(new HttpPollConfig<Boolean>(SERVICE_UP)
                        .suburl(getConfig(NodeJsWebAppService.SERVICE_UP_PATH))
                        .checkSuccess(Predicates.alwaysTrue())
                        .onSuccess(HttpValueFunctions.responseCodeEquals(200))
                        .setOnException(false))
                .build();

        WebAppServiceMethods.connectWebAppServerPolicies(this);
    }

    @Override
    public void disconnectSensors() {
        if (httpFeed != null) httpFeed.stop();
        super.disconnectSensors();
    }

    public NodeJsWebAppDriver getDriver() {
        return (NodeJsWebAppDriver) super.getDriver();
    }

    @Override
    protected void doStop() {
        super.doStop();
        setAttribute(REQUESTS_PER_SECOND_LAST, 0D);
        setAttribute(REQUESTS_PER_SECOND_IN_WINDOW, 0D);
    }

    @Override
    public Class<?> getDriverInterface() {
        return NodeJsWebAppDriver.class;
    }

}
