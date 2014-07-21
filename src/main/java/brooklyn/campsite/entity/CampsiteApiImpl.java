package brooklyn.campsite.entity;

import brooklyn.entity.webapp.nodejs.NodeJsWebAppDriver;
import brooklyn.entity.webapp.nodejs.NodeJsWebAppServiceImpl;

public class CampsiteApiImpl extends NodeJsWebAppServiceImpl implements CampsiteApi {

    @Override
    public Class<?> getDriverInterface() {
        return NodeJsWebAppDriver.class;
    }

}
