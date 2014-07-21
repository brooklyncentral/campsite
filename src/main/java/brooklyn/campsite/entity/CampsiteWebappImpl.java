/*
 * Copyright 2014 by Cloudsoft Corporation Limited
 */
package brooklyn.campsite.entity;

import java.util.Map;

import org.jclouds.compute.domain.OsFamily;

import brooklyn.entity.basic.SoftwareProcessImpl;
import brooklyn.event.feed.http.HttpFeed;
import brooklyn.event.feed.http.HttpPollConfig;
import brooklyn.event.feed.http.HttpValueFunctions;
import brooklyn.location.MachineProvisioningLocation;
import brooklyn.location.access.BrooklynAccessUtils;
import brooklyn.location.jclouds.JcloudsLocationConfig;
import brooklyn.util.collections.MutableMap;

import com.google.common.base.Predicates;
import com.google.common.net.HostAndPort;

public class CampsiteWebappImpl extends SoftwareProcessImpl implements CampsiteWebapp {

    private transient HttpFeed httpFeed;

    @Override
    public void init() {
        // TODO
    }

    @Override
    public Class getDriverInterface() {
        return CampsiteWebappDriver.class;
    }

    @Override
    public CampsiteWebappDriver getDriver() {
        return (CampsiteWebappDriver) super.getDriver();
    }

    @Override
    protected void connectSensors() {
        super.connectSensors();

        HostAndPort accessible = BrooklynAccessUtils.getBrooklynAccessibleAddress(this, getAttribute(HTTP_PORT));
        String diffusionUrl = String.format("http://%s:%d/", accessible.getHostText(), accessible.getPort());
        httpFeed = HttpFeed.builder()
                .entity(this)
                .baseUri(diffusionUrl)
                .poll(new HttpPollConfig<Boolean>(SERVICE_UP)
                        .checkSuccess(Predicates.alwaysTrue())
                        .onSuccess(HttpValueFunctions.responseCodeEquals(200))
                        .setOnException(false))
                .build();
    }

    @Override
    public void disconnectSensors() {
        if (httpFeed != null) httpFeed.stop();
        super.disconnectSensors();
    }

    @Override
    protected Map<String,Object> obtainProvisioningFlags(MachineProvisioningLocation location) {
        Map<String,Object> flags = MutableMap.<String,Object>builder()
                .putAll(super.obtainProvisioningFlags(location))
                .put(JcloudsLocationConfig.OS_FAMILY.getName(), OsFamily.UBUNTU)
                .put(JcloudsLocationConfig.OS_64_BIT.getName(), true)
                .put(JcloudsLocationConfig.MIN_RAM.getName(), 2048)
                .put(JcloudsLocationConfig.MIN_CORES.getName(), 2)
                .build();
        return flags;
    }

    public Integer getHttpPort() { return getAttribute(HTTP_PORT); }

    @Override
    public String getSiteDomainName() { return getConfig(SITE_DOMAIN_NAME); }

}
