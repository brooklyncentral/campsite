package brooklyn.campsite.entity;

import brooklyn.config.ConfigKey;
import brooklyn.entity.basic.ConfigKeys;
import brooklyn.util.flags.SetFromFlag;

/**
 * Campsite configuration.
 */
public interface CampsiteConfig {

    @SetFromFlag("databaseHostAndPort")
    ConfigKey<String> DATABASE_HOST_AND_PORT = ConfigKeys.newStringConfigKey("campsite.database.hostandport", "MySQL database host and port");

    @SetFromFlag("databaseHost")
    ConfigKey<String> DATABASE_HOST = ConfigKeys.newStringConfigKey("campsite.database.host", "MySQL database host", "localhost");

    @SetFromFlag("databasePort")
    ConfigKey<Integer> DATABASE_PORT = ConfigKeys.newIntegerConfigKey("campsite.database.port", "MySQL database port", 3306);

    @SetFromFlag("databaseUser")
    ConfigKey<String> DATABASE_USER = ConfigKeys.newStringConfigKey("campsite.database.user", "MySQL database user", "root");

    @SetFromFlag("databasePassword")
    ConfigKey<String> DATABASE_PASSWORD = ConfigKeys.newStringConfigKey("campsite.database.password", "MySQL database password", "password");

    @SetFromFlag("mailerUser")
    ConfigKey<String> MAILER_USER = ConfigKeys.newStringConfigKey("campsite.mailer.user", "Mailer user", "");

    @SetFromFlag("mailerPassword")
    ConfigKey<String> MAILER_PASSWORD = ConfigKeys.newStringConfigKey("campsite.mailer.password", "Mailer password", "");

    @SetFromFlag("recaptchaPublicKey")
    ConfigKey<String> RECAPTCHA_PUBLIC_KEY = ConfigKeys.newStringConfigKey("campsite.recaptcha.publicKey", "Re-Captcha public key", "");

    @SetFromFlag("recaptchaPrivateKey")
    ConfigKey<String> RECAPTCHA_PRIVATE_KEY = ConfigKeys.newStringConfigKey("campsite.recaptcha.privateKey", "Re-Captcha private key", "");

    @SetFromFlag("objectStorage")
    ConfigKey<String> OBJECT_STORAGE = ConfigKeys.newStringConfigKey("campsite.objectStorage", "Object storage (HpObjectStorage or AWSObjectStorage)", "AWSObjectStorage");

    @SetFromFlag("s3AccessKey")
    ConfigKey<String> S3_ACCESS_KEY = ConfigKeys.newStringConfigKey("campsite.s3.accessKey", "S3 access key", "");

    @SetFromFlag("s3SecretKey")
    ConfigKey<String> S3_SECRET_KEY = ConfigKeys.newStringConfigKey("campsite.s3.secretKey", "S3 secret key", "");

    @SetFromFlag("s3BucketName")
    ConfigKey<String> S3_BUCKET_NAME = ConfigKeys.newStringConfigKey("campsite.s3.bucketName", "S3 public bucket name", "campsite-public");

    @SetFromFlag("s3PrivateBucketName")
    ConfigKey<String> S3_PRIVATE_BUCKET_NAME = ConfigKeys.newStringConfigKey("campsite.s3.bucketname.private", "S3 private bucket name", "campsite-private");

    @SetFromFlag("hpcloudAccessKey")
    ConfigKey<String> HP_CLOUD_ACCESS_KEY = ConfigKeys.newStringConfigKey("campsite.hpcloud.accesskey", "HP Cloud access key", "");

    @SetFromFlag("hpcloudSecretKey")
    ConfigKey<String> HP_CLOUD_SECRET_KEY = ConfigKeys.newStringConfigKey("campsite.hpcloud.secretKey", "HP Cloud secret key", "");

    @SetFromFlag("hpcloudTenantId")
    ConfigKey<String> HP_CLOUD_TENANT_ID = ConfigKeys.newStringConfigKey("campsite.hpcloud.tenantId", "HP Cloud tenant ID", "");

    @SetFromFlag("sendgridUser")
    ConfigKey<String> SENDGRID_USER = ConfigKeys.newStringConfigKey("campsite.sendgrid.user", "SendGrid user", "");

    @SetFromFlag("sendgridPassword")
    ConfigKey<String> SENDGRID_PASSWORD = ConfigKeys.newStringConfigKey("campsite.sendgrid.password", "SendGrid password", "");

    @SetFromFlag("queueService")
    ConfigKey<String> QUEUE_SERVICE = ConfigKeys.newStringConfigKey("campsite.queueService", "Queue service (RabbitMQ, AWS_SQS or HPCloud)", "AWS_SQS");

    @SetFromFlag("rabbitHostAndPort")
    ConfigKey<String> RABBIT_HOST_AND_PORT = ConfigKeys.newStringConfigKey("campsite.rabbit.hostandport", "RabbitMQ hostname and port number");

    @SetFromFlag("rabbitHost")
    ConfigKey<String> RABBIT_HOST = ConfigKeys.newStringConfigKey("campsite.rabbit.host", "RabbitMQ hostname", "localhost");

    @SetFromFlag("rabbitPort")
    ConfigKey<Integer> RABBIT_PORT = ConfigKeys.newIntegerConfigKey("campsite.rabbit.port", "RabbitMQ port", 5672);

    @SetFromFlag("rabbitUser")
    ConfigKey<String> RABBIT_USER = ConfigKeys.newStringConfigKey("campsite.rabbit.user", "RabbitMQ user", "");

    @SetFromFlag("rabbitPassword")
    ConfigKey<String> RABBIT_PASSWORD = ConfigKeys.newStringConfigKey("campsite.rabbit.password", "RabbitMQ password", "");

}
