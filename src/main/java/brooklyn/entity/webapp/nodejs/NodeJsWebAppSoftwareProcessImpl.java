package brooklyn.entity.webapp.nodejs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import brooklyn.entity.basic.SoftwareProcessImpl;
import brooklyn.entity.webapp.WebAppServiceMethods;

public abstract class NodeJsWebAppSoftwareProcessImpl extends SoftwareProcessImpl implements NodeJsWebAppSoftwareProcess {

    private static final Logger LOG = LoggerFactory.getLogger(NodeJsWebAppSoftwareProcessImpl.class);

    public NodeJsWebAppSoftwareProcessImpl() {
        super();
    }

    @Override
    protected void connectSensors() {
        super.connectSensors();

        WebAppServiceMethods.connectWebAppServerPolicies(this);
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

}
