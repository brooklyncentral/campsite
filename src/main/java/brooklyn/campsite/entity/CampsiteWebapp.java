/*
 * Copyright 2014 by Cloudsoft Corporation Limited
 */
package brooklyn.campsite.entity;

import brooklyn.config.ConfigKey;
import brooklyn.entity.basic.ConfigKeys;
import brooklyn.entity.basic.SoftwareProcess;
import brooklyn.entity.proxying.ImplementedBy;
import brooklyn.entity.webapp.WebAppService;
import brooklyn.event.AttributeSensor;
import brooklyn.event.basic.AttributeSensorAndConfigKey;
import brooklyn.event.basic.Sensors;
import brooklyn.util.flags.SetFromFlag;

@ImplementedBy(CampsiteWebappImpl.class)
public interface CampsiteWebapp extends SoftwareProcess, WebAppService, CampsiteConfig {

    @SetFromFlag("gitRepoUrl")
    ConfigKey<String> GIT_REPOSITORY_URL = ConfigKeys.newStringConfigKey("campsite.github.url", "URL of GitHub repository");

    @SetFromFlag("archiveUrl")
    ConfigKey<String> ARCHIVE_DOWNLOAD_URL = ConfigKeys.newStringConfigKey("campsite.archive.url", "URL of archive file",
            "https://s3-eu-west-1.amazonaws.com/brooklyn-campsite/spoutlet-master.zip");

    AttributeSensor<String> BASE_DIR = Sensors.newStringSensor("campsite.base.dir", "Campsite base directory");

    @SetFromFlag("parametersTemplate")
    AttributeSensorAndConfigKey<String, String> PARAMETERS_INI_TEMPLATE_URL = ConfigKeys.newStringSensorAndConfigKey(
            "campsite.parameters.url", "Campsite parameters.ini template file (in freemarker format)", 
            "classpath://brooklyn/campsite/parameters.ini");

    @SetFromFlag("phpunitTemplate")
    AttributeSensorAndConfigKey<String, String> PHPUNIT_XML_TEMPLATE_URL = ConfigKeys.newStringSensorAndConfigKey(
            "campsite.phpunit.url", "Campsite PHP configuration XML template file (in freemarker format)", 
            "classpath://brooklyn/campsite/phpunit.xml");

    @SetFromFlag("vhostTemplate")
    AttributeSensorAndConfigKey<String, String> VHOST_TEMPLATE_URL = ConfigKeys.newStringSensorAndConfigKey(
            "campsite.vhost.url", "Campsite vhost template file (in freemarker format)", 
            "classpath://brooklyn/campsite/vhost");

    @SetFromFlag("vhostTemplate")
    AttributeSensorAndConfigKey<String, String> VHOST_SSL_TEMPLATE_URL = ConfigKeys.newStringSensorAndConfigKey(
            "campsite.vhost.url", "Campsite SSL vhost template file (in freemarker format)", 
            "classpath://brooklyn/campsite/vhost.ssl");

    @SetFromFlag("timezone")
    ConfigKey<String> TIMEZONE = ConfigKeys.newStringConfigKey("campsite.timezone", "Campsite site time zone", "Europe/London");

    @SetFromFlag("domainName")
    ConfigKey<String> SITE_DOMAIN_NAME = ConfigKeys.newStringConfigKey("campsite.domainName", "Campsite site domain name", "campsite.org");

    AttributeSensor<Boolean> CLUSTERED = Sensors.newBooleanSensor("campsite.webapp.clustered", "Set if this is a clustered Campsite webapp");
    AttributeSensor<Boolean> FIRST = Sensors.newBooleanSensor("campsite.webapp.first", "Set if this is the first Campsite webapp in a cluster");

    Integer getHttpPort();
    Integer getHttpsPort();

    String getSiteDomainName();

}
