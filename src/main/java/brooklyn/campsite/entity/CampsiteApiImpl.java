package brooklyn.campsite.entity;

import brooklyn.entity.basic.Entities;
import brooklyn.entity.webapp.nodejs.NodeJsWebAppServiceImpl;
import brooklyn.util.time.Duration;

public class CampsiteApiImpl extends NodeJsWebAppServiceImpl implements CampsiteApi {

    @Override
    public Class<?> getDriverInterface() {
        return CampsiteApiDriver.class;
    }

    @Override
    protected void preStart() {
        Entities.waitForServiceUp(getConfig(CAMPSITE_WEBAPP), Duration.minutes(30));
    }

}
