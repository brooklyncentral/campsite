package brooklyn.campsite.entity;

import java.util.List;

import brooklyn.config.ConfigKey;
import brooklyn.entity.Entity;
import brooklyn.entity.basic.ConfigKeys;
import brooklyn.entity.proxying.ImplementedBy;
import brooklyn.entity.webapp.nodejs.NodeJsWebAppService;
import brooklyn.util.flags.SetFromFlag;

import com.google.common.collect.ImmutableList;

@ImplementedBy(CampsiteApiImpl.class)
public interface CampsiteApi extends NodeJsWebAppService {

    ConfigKey<String> APP_ARCHIVE_URL = ConfigKeys.newConfigKeyWithDefault(NodeJsWebAppService.APP_ARCHIVE_URL, "https://s3-eu-west-1.amazonaws.com/brooklyn-campsite/node-api.tgz");
    ConfigKey<String> APP_FILE = ConfigKeys.newConfigKeyWithDefault(NodeJsWebAppService.APP_FILE, "api.js");
    ConfigKey<String> APP_NAME = ConfigKeys.newConfigKeyWithDefault(NodeJsWebAppService.APP_NAME, "node-api");
    ConfigKey<String> APP_COMMAND = ConfigKeys.newConfigKeyWithDefault(NodeJsWebAppService.APP_COMMAND, "forever start");
    ConfigKey<List<String>> NODE_PACKAGE_LIST = ConfigKeys.newConfigKeyWithDefault(NodeJsWebAppService.NODE_PACKAGE_LIST, ImmutableList.of("forever", "restify", "knex", "mysql", "underscore"));
    ConfigKey<String> SERVICE_UP_PATH = ConfigKeys.newConfigKeyWithDefault(NodeJsWebAppService.SERVICE_UP_PATH, "/users");

    ConfigKey<Integer> API_PORT = ConfigKeys.newIntegerConfigKey("campsite.api.port", "Campsite API  port number", 3000);

    ConfigKey<Entity> CAMPSITE_WEBAPP = ConfigKeys.newConfigKey(Entity.class, "campsite.webapp", "Campsite web application");

    @SetFromFlag("databaseHostAndPort")
    ConfigKey<String> DATABASE_HOST_AND_PORT = CampsiteConfig.DATABASE_HOST_AND_PORT;

    @SetFromFlag("databaseHost")
    ConfigKey<String> DATABASE_HOST = CampsiteConfig.DATABASE_HOST;

    @SetFromFlag("databasePort")
    ConfigKey<Integer> DATABASE_PORT = CampsiteConfig.DATABASE_PORT;

    @SetFromFlag("databaseUser")
    ConfigKey<String> DATABASE_USER = CampsiteConfig.DATABASE_USER;

    @SetFromFlag("databasePassword")
    ConfigKey<String> DATABASE_PASSWORD = CampsiteConfig.DATABASE_PASSWORD;

    @SetFromFlag("appUser")
    ConfigKey<String> CONFIG_JSON_TEMPLATE_URL = ConfigKeys.newStringConfigKey("campsite.api.configUrl", "The Campsite API JSON configuration (FreeMarker template URL)",
            "classpath://brooklyn/campsite/config_data.json");

}
