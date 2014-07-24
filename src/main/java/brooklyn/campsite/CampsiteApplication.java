/*
 * Copyright 2014 by Cloudsoft Corporation Limited
 */
package brooklyn.campsite;

import static brooklyn.event.basic.DependentConfiguration.attributeWhenReady;
import brooklyn.campsite.entity.CampsiteApi;
import brooklyn.campsite.entity.CampsiteConfig;
import brooklyn.campsite.entity.CampsiteWebapp;
import brooklyn.catalog.Catalog;
import brooklyn.catalog.CatalogConfig;
import brooklyn.config.ConfigKey;
import brooklyn.entity.basic.AbstractApplication;
import brooklyn.entity.basic.Attributes;
import brooklyn.entity.basic.ConfigKeys;
import brooklyn.entity.basic.SoftwareProcess;
import brooklyn.entity.basic.StartableApplication;
import brooklyn.entity.database.mysql.MySqlNode;
import brooklyn.entity.messaging.rabbit.RabbitBroker;
import brooklyn.entity.proxying.EntitySpec;
import brooklyn.location.basic.PortRanges;
import brooklyn.util.collections.MutableMap;

/**
 * Clustered Diffusion servers.
 */
@Catalog(name="Campsite",
        description="Campsite Application.",
        iconUrl="classpath://campsite-logo.png")
public class CampsiteApplication extends AbstractApplication implements StartableApplication {

    @CatalogConfig(label ="Object storage (HpObjectStorage or AWSObjectStorage)", priority = 64)
    public static final ConfigKey<String> OBJECT_STORAGE = ConfigKeys.newConfigKeyWithPrefix("catalog.", CampsiteConfig.OBJECT_STORAGE);
    @CatalogConfig(label = "Queue service (RabbitMQ, AWS_SQS or HPCloud)", priority = 62)
    public static final ConfigKey<String> QUEUE_SERVICE = ConfigKeys.newConfigKeyWithPrefix("catalog.", CampsiteConfig.QUEUE_SERVICE);
    @CatalogConfig(label = "Email service (SendGrid or AWS_SES)", priority = 60)
    public static final ConfigKey<String> EMAIL_SERVICE = ConfigKeys.newConfigKeyWithPrefix("catalog.", CampsiteConfig.EMAIL_SERVICE);

    @CatalogConfig(label = "Mailer user", priority = 52)
    public static final ConfigKey<String> MAILER_USER = ConfigKeys.newConfigKeyWithPrefix("catalog.", CampsiteConfig.MAILER_USER);
    @CatalogConfig(label = "Mailer password", priority = 50)
    public static final ConfigKey<String> MAILER_PASSWORD = ConfigKeys.newConfigKeyWithPrefix("catalog.", CampsiteConfig.MAILER_PASSWORD);

    @CatalogConfig(label = "Re-Captche public key", priority = 42)
    public static final ConfigKey<String> RECAPTCHA_PUBLIC_KEY = ConfigKeys.newConfigKeyWithPrefix("catalog.", CampsiteConfig.RECAPTCHA_PUBLIC_KEY);
    @CatalogConfig(label = "Re-Captcha private key", priority = 40)
    public static final ConfigKey<String> RECAPTCHA_PRIVATE_KEY = ConfigKeys.newConfigKeyWithPrefix("catalog.", CampsiteConfig.RECAPTCHA_PRIVATE_KEY);

    @CatalogConfig(label = "S3 access key", priority = 36)
    public static final ConfigKey<String> S3_ACCESS_KEY = ConfigKeys.newConfigKeyWithPrefix("catalog.", CampsiteConfig.S3_ACCESS_KEY);
    @CatalogConfig(label = "S3 secret key", priority = 34)
    public static final ConfigKey<String> S3_SECRET_KEY = ConfigKeys.newConfigKeyWithPrefix("catalog.", CampsiteConfig.S3_SECRET_KEY);
    @CatalogConfig(label = "S3 public bucket name", priority = 32)
    public static final ConfigKey<String> S3_BUCKET_NAME = ConfigKeys.newConfigKeyWithPrefix("catalog.", CampsiteConfig.S3_BUCKET_NAME);
    @CatalogConfig(label = "S3 private bucket name", priority = 30)
    public static final ConfigKey<String> S3_PRIVATE_BUCKET_NAME = ConfigKeys.newConfigKeyWithPrefix("catalog.", CampsiteConfig.S3_PRIVATE_BUCKET_NAME);

    @CatalogConfig(label = "HP Cloud access key", priority = 24)
    public static final ConfigKey<String> HP_CLOUD_ACCESS_KEY = ConfigKeys.newConfigKeyWithPrefix("catalog.", CampsiteConfig.HP_CLOUD_ACCESS_KEY);
    @CatalogConfig(label = "HP Cloud secret key", priority = 22)
    public static final ConfigKey<String> HP_CLOUD_SECRET_KEY = ConfigKeys.newConfigKeyWithPrefix("catalog.", CampsiteConfig.HP_CLOUD_SECRET_KEY);
    @CatalogConfig(label = "HP Cloud tenant ID", priority = 20)
    public static final ConfigKey<String> HP_CLOUD_TENANT_ID = ConfigKeys.newConfigKeyWithPrefix("catalog.", CampsiteConfig.HP_CLOUD_TENANT_ID);

    @CatalogConfig(label = "SendGrid user", priority = 12)
    public static final ConfigKey<String> SENDGRID_USER = ConfigKeys.newConfigKeyWithPrefix("catalog.", CampsiteConfig.SENDGRID_USER);
    @CatalogConfig(label = "SendGrid password", priority = 10)
    public static final ConfigKey<String> SENDGRID_PASSWORD = ConfigKeys.newConfigKeyWithPrefix("catalog.", CampsiteConfig.SENDGRID_PASSWORD);

    @Override
    public void init() {
        String queueService = getConfig(QUEUE_SERVICE);

        MySqlNode mysql = addChild(EntitySpec.create(MySqlNode.class)
                .configure(MySqlNode.CREATION_SCRIPT_URL, "classpath://brooklyn/campsite/create-campsite-user.sql"));

        EntitySpec<CampsiteWebapp> campspec = EntitySpec.create(CampsiteWebapp.class)
                .configure(SoftwareProcess.PROVISIONING_PROPERTIES, MutableMap.<String, Object>of("os64Bit", "true", "minRam", "7000"))
                .configure(Attributes.HTTP_PORT, PortRanges.fromInteger(80))
                .configure(CampsiteConfig.OBJECT_STORAGE, getConfig(OBJECT_STORAGE))
                .configure(CampsiteConfig.QUEUE_SERVICE, getConfig(QUEUE_SERVICE))
                .configure(CampsiteConfig.MAILER_USER, getConfig(MAILER_USER))
                .configure(CampsiteConfig.MAILER_PASSWORD, getConfig(MAILER_PASSWORD))
                .configure(CampsiteConfig.RECAPTCHA_PUBLIC_KEY, getConfig(RECAPTCHA_PUBLIC_KEY))
                .configure(CampsiteConfig.RECAPTCHA_PRIVATE_KEY, getConfig(RECAPTCHA_PRIVATE_KEY))
                .configure(CampsiteConfig.S3_ACCESS_KEY, getConfig(S3_ACCESS_KEY))
                .configure(CampsiteConfig.S3_SECRET_KEY, getConfig(S3_SECRET_KEY))
                .configure(CampsiteConfig.S3_BUCKET_NAME, getConfig(S3_BUCKET_NAME))
                .configure(CampsiteConfig.S3_PRIVATE_BUCKET_NAME, getConfig(S3_PRIVATE_BUCKET_NAME))
                .configure(CampsiteConfig.HP_CLOUD_ACCESS_KEY, getConfig(HP_CLOUD_ACCESS_KEY))
                .configure(CampsiteConfig.HP_CLOUD_SECRET_KEY, getConfig(HP_CLOUD_SECRET_KEY))
                .configure(CampsiteConfig.HP_CLOUD_TENANT_ID, getConfig(HP_CLOUD_TENANT_ID))
                .configure(CampsiteConfig.EMAIL_SERVICE, getConfig(EMAIL_SERVICE))
                .configure(CampsiteConfig.SENDGRID_USER, getConfig(SENDGRID_USER))
                .configure(CampsiteConfig.SENDGRID_PASSWORD, getConfig(SENDGRID_PASSWORD))
                .configure(CampsiteConfig.DATABASE_HOST, attributeWhenReady(mysql, Attributes.HOSTNAME))
                .configure(CampsiteConfig.DATABASE_PORT, attributeWhenReady(mysql, MySqlNode.MYSQL_PORT))
                .configure(CampsiteConfig.DATABASE_USER, "campsite")
                .configure(CampsiteConfig.DATABASE_PASSWORD, "p4ssw0rd");

        if ("RabbitMQ".equals(queueService)) {
            RabbitBroker rabbit = addChild(EntitySpec.create(RabbitBroker.class));

            campspec.configure(CampsiteConfig.RABBIT_HOST, attributeWhenReady(rabbit, Attributes.HOSTNAME))
                    .configure(CampsiteConfig.RABBIT_PORT, attributeWhenReady(rabbit, RabbitBroker.AMQP_PORT));
        }

        CampsiteWebapp campsite = addChild(campspec);

        CampsiteApi api = addChild(EntitySpec.create(CampsiteApi.class)
                .configure(CampsiteApi.CAMPSITE_WEBAPP, campsite)
                .configure(CampsiteConfig.DATABASE_HOST, attributeWhenReady(mysql, Attributes.HOSTNAME))
                .configure(CampsiteConfig.DATABASE_PORT, attributeWhenReady(mysql, MySqlNode.MYSQL_PORT))
                .configure(CampsiteConfig.DATABASE_USER, "campsite")
                .configure(CampsiteConfig.DATABASE_PASSWORD, "p4ssw0rd"));

    }

}
