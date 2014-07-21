package brooklyn.entity.webapp.nodejs;

import brooklyn.entity.basic.SoftwareProcessImpl;
import brooklyn.entity.webapp.WebAppServiceMethods;

public class NodeJsWebAppServiceImpl extends SoftwareProcessImpl implements NodeJsWebAppService {

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

    @Override
    public Class<?> getDriverInterface() {
        return NodeJsWebAppDriver.class;
    }

}
