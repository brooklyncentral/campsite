/*
 * Copyright 2014 by Cloudsoft Corporation Limited
 */
package brooklyn.campsite.entity;

import com.google.common.net.HostAndPort;

import brooklyn.entity.basic.SoftwareProcessDriver;

public interface CampsiteWebappDriver extends SoftwareProcessDriver {

    String getBaseDir();
    Integer getHttpPort();
    Integer getHttpsPort();

    HostAndPort getDatabaseHostAndPort();
    String getDatabaseHost();
    Integer getDatabasePort();
    String getDatabaseUser();
    String getDatabasePassword();
    String getMailerUser();
    String getMailerPassword();
    String getRecaptchaPublicKey();
    String getRecaptchaPrivateKey();
    String getObjectStorage();
    String getS3AccessKey();
    String getS3SecretKey();
    String getS3BucketName();
    String getS3PrivateBucketName();
    String getHpcloudAccessKey();
    String getHpcloudSecretKey();
    String getHpcloudTenantId();
    String getEmailService();
    String getSendgridUsername();
    String getSendgridPassword();
    String getQueueService();
    HostAndPort getRabbitHostAndPort();
    String getRabbitHost();
    Integer getRabbitPort();
    String getRabbitUser();
    String getRabbitPassword();
    HostAndPort getMemcachedHostAndPort();
    String getMemcachedHost();
    Integer getMemcachedPort();

}
