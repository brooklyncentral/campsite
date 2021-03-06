/*
 * Copyright 2014 by Cloudsoft Corporation Limited
 */
package brooklyn.campsite.entity;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import brooklyn.entity.basic.AbstractSoftwareProcessSshDriver;
import brooklyn.entity.basic.Attributes;
import brooklyn.entity.basic.EntityLocal;
import brooklyn.entity.software.SshEffectorTasks;
import brooklyn.entity.webapp.WebAppService;
import brooklyn.location.basic.SshMachineLocation;
import brooklyn.util.collections.MutableList;
import brooklyn.util.collections.MutableMap;
import brooklyn.util.file.ArchiveUtils;
import brooklyn.util.net.Networking;
import brooklyn.util.os.Os;
import brooklyn.util.ssh.BashCommands;
import brooklyn.util.task.DynamicTasks;
import brooklyn.util.task.system.ProcessTaskWrapper;
import brooklyn.util.text.Strings;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.net.HostAndPort;

public class CampsiteWebappSshDriver extends AbstractSoftwareProcessSshDriver implements CampsiteWebappDriver {

    private static final Logger LOG = LoggerFactory.getLogger(CampsiteWebapp.class);

    public CampsiteWebappSshDriver(EntityLocal entity, SshMachineLocation machine) {
        super(entity, machine);

        entity.setAttribute(CampsiteWebapp.BASE_DIR, getBaseDir());
    }

    protected String getLogFileLocation() {
        return Os.mergePaths(getBaseDir(), "logs", "console.log");
    }

    @Override
    public String getBaseDir() {
        return Os.mergePaths(getRunDir(), "campsite");
    }

    @Override
    public Integer getHttpPort() {
        return getEntity().getAttribute(Attributes.HTTP_PORT);
    }

    @Override
    public Integer getHttpsPort() {
        return getEntity().getAttribute(Attributes.HTTPS_PORT);
    }

    @Override
    public void postLaunch() {
        String rootUrl = String.format("http://%s:%d/", getHostname(), getHttpPort());
        entity.setAttribute(WebAppService.ROOT_URL, rootUrl);
    }

    @Override
    public Set<Integer> getPortsUsed() {
        return ImmutableSet.<Integer>builder()
                .addAll(super.getPortsUsed())
                .addAll(getPortMap().values())
                .build();
    }

    protected Map<String, Integer> getPortMap() {
        Map<String, Integer> ports = Maps.newHashMap();
        ports.put("http", getEntity().getAttribute(Attributes.HTTP_PORT));
        if (isSslEnabled()) ports.put("https", getEntity().getAttribute(Attributes.HTTPS_PORT));
        return ImmutableMap.copyOf(ports);
    }

    protected boolean isSslEnabled() {
        return getEntity().getAttribute(WebAppService.ENABLED_PROTOCOLS).contains("https");
    }

    @Override
    public void install() {
        List<String> commands = Lists.newLinkedList();
        // TODO add memcached entity
        commands.add(BashCommands.installPackage("apache2 ssl-cert netcat git-core ftp-upload ncurses-term memcached"));

        String gitRepoUrl = getEntity().getConfig(CampsiteWebapp.GIT_REPOSITORY_URL);
        String archiveUrl = getEntity().getConfig(CampsiteWebapp.ARCHIVE_DOWNLOAD_URL);

        if (Strings.isNonBlank(gitRepoUrl) && Strings.isNonBlank(archiveUrl)) {
            throw new IllegalStateException("Only one of Git or archive URL must be set");
        } else if (Strings.isNonBlank(gitRepoUrl)) {
            commands.add(String.format("git clone %s spoutlet-master", gitRepoUrl));
        } else if (Strings.isNonBlank(archiveUrl)) {
            ArchiveUtils.deploy(archiveUrl, getMachine(), getInstallDir());
        } else {
            throw new IllegalStateException("At least one of Git or archive URL must be set");
        }

        // Install PHP (APT only) 
        commands.add(BashCommands.ifExecutableElse0("apt-get", BashCommands.sudo("add-apt-repository ppa:ondrej/php5-oldstable")));
        commands.add(BashCommands.installPackage("libapache2-mod-php5 php5-intl php-apc php5-curl php5-gd php5-mysql php5-mcrypt " +
                "php5-memcache php5-memcached php5-sqlite php5-xdebug mysql-client php-pear"));

        newScript(INSTALLING)
                .body.append(commands)
                .execute();
    }

    @Override
    public void customize() {
        Networking.checkPortsValid(getPortMap());

        newScript(CUSTOMIZING)
                .updateTaskAndFailOnNonZeroResultCode()
                .body.append(
                        String.format("cp -R %s %s", Os.mergePaths(getInstallDir(), "spoutlet-master"), getBaseDir()),
                        String.format("sed -i.bak " +
                                "-e 's/campsite\\.org/%s/g' " +
                                "-e 's/localhost 11211/%s %d/g' " +
                                Os.mergePaths(getBaseDir(), "misc_scripts", "first_time_setup.sh"),
                                        getEntity().getConfig(CampsiteWebapp.SITE_DOMAIN_NAME), getMemcachedHost(), getMemcachedPort()),
                        BashCommands.sudo(String.format("sed -i.bak " +
                                "-e 's/date\\.timezone.*=.*$/date.timezone = \"%s\"/g' " +
                                "-e 's/short_open_tag.*=.*$/short_open_tag = Off/g' " +
                                "/etc/php5/apache2/php.ini",
                                        Strings.replaceAll(getEntity().getConfig(CampsiteWebapp.TIMEZONE), "/", "\\/"))))
                .execute();

        copyTemplate(entity.getConfig(CampsiteWebapp.PARAMETERS_INI_TEMPLATE_URL), Os.mergePaths(getBaseDir(), "app", "config", "parameters.ini"));
        copyTemplate(entity.getConfig(CampsiteWebapp.PHPUNIT_XML_TEMPLATE_URL), Os.mergePaths(getBaseDir(), "app", "phpunit.xml"));

        List<String> commands = Lists.newLinkedList();
        if (isSslEnabled()) {
            // SSL Site
            copyTemplate(entity.getConfig(CampsiteWebapp.VHOST_SSL_TEMPLATE_URL), Os.mergePaths(getRunDir(), "vhost.ssl"));
            commands.add(BashCommands.sudo(String.format("cp %s %s", Os.mergePaths(getRunDir(), "vhost.ssl"), "/etc/apache2/sites-available/campsite.ssl")));
            commands.add(BashCommands.sudo("mkdir -p /etc/apache2/ssl"));
            String certificateSourceUrl = getEntity().getConfig(CampsiteWebapp.SSL_CERTIFICATE_SOURCE_URL);
            String keySourceUrl = getEntity().getConfig(CampsiteWebapp.SSL_KEY_SOURCE_URL);
            if (Strings.isNonBlank(certificateSourceUrl) && Strings.isNonBlank(keySourceUrl)) {
                copyResource(certificateSourceUrl, "/etc/apache2/ssl/apache.crt");
                copyResource(keySourceUrl, "/etc/apache2/ssl/apache.key");
            } else {
                // Create a temporary self-signed 'snake oil' certificate
                commands.add(BashCommands.sudo(String.format("openssl req -new -newkey rsa:4096 -days 365 -nodes -x509 " +
                        "-subj \"/C=AA/ST=None/L=None/O=None/CN=%s\" " +
                        "-keyout /etc/apache2/ssl/apache.key -out /etc/apache2/ssl/apache.crt",
                                getEntity().getConfig(CampsiteWebapp.SITE_DOMAIN_NAME))));
            }
            commands.add(BashCommands.sudo("a2ensite campsite.ssl"));
            commands.add(BashCommands.sudo("a2enmod ssl"));
        }

        // Campsite
        copyTemplate(entity.getConfig(CampsiteWebapp.VHOST_TEMPLATE_URL), Os.mergePaths(getRunDir(), "vhost"));
        commands.addAll(MutableList.of(
                BashCommands.sudo(String.format("cp %s %s", Os.mergePaths(getRunDir(), "vhost"), "/etc/apache2/sites-available/campsite")),
                BashCommands.sudo("a2ensite campsite")));

        // Common commands
        commands.addAll(MutableList.of(
                BashCommands.sudo("a2dissite default"),
                BashCommands.sudo("a2enmod rewrite"),
                "cd campsite",
                "mkdir app/cache",
                "chmod 777 app/cache",
                "export ENV=prod",
                "php bin/vendors install", // XXX hack due to unreliable git clone
                "php bin/vendors install"));

        // Configure database on first server only
        Boolean first = getEntity().getAttribute(CampsiteWebapp.FIRST);
        if (first) {
            commands.addAll(MutableList.of(
                    "php app/console doctrine:database:create",
                    "php app/console doctrine:mig:mig --no-interaction",
                    "php app/console doctrine:database:create --connection=acl --env=prod",
                    "php app/console init:acl --env=prod",
                    BashCommands.ok("./misc_scripts/first_time_setup.sh"), // FIXME fails in Clocker
                    "php app/console assetic:dump --env=prod --no-debug",
                    "./misc_scripts/updateFeedbackTables.sh"));
        }

        // Install themes
        commands.addAll(MutableList.of(
                "php app/console themes:install web --symlink",
                "php app/console assets:install web --symlink"));

        newScript(MutableMap.of(DEBUG, true), CUSTOMIZING)
                .updateTaskAndFailOnNonZeroResultCode()
                .body.append(commands)
                .execute();

        // Setup cache
        ProcessTaskWrapper<Integer> task = SshEffectorTasks.ssh(
                        "cd " + getBaseDir(),
                        BashCommands.sudo("rm -rf app/cache/*"),
                        "php app/console cache:clear --env=prod --no-debug")
                .machine(getMachine())
                .summary("Clear and warm up cache")
                .newTask();
        DynamicTasks.queueIfPossible(task).orSubmitAsync(getEntity());
    }

    @Override
    public void launch() {
        newScript(MutableMap.of(USE_PID_FILE, false), LAUNCHING)
                .updateTaskAndFailOnNonZeroResultCode()
                .body.append(BashCommands.sudo("service apache2 restart"))
                .execute();
    }

    @Override
    public boolean isRunning() {
        return newScript(MutableMap.of(USE_PID_FILE, false), CHECK_RUNNING)
                .body.append(BashCommands.sudo("service apache2 status"))
                .execute() == 0;
    }

    @Override
    public void stop() {
        newScript(MutableMap.of(USE_PID_FILE, false), STOPPING)
                .body.append(BashCommands.sudo("service apache2 stop"))
                .execute();
    }

    @Override
    public HostAndPort getDatabaseHostAndPort() {
        String databaseHostAndPort = getEntity().getConfig(CampsiteConfig.DATABASE_HOST_AND_PORT);
        if (Strings.isNonBlank(databaseHostAndPort)) {
            return HostAndPort.fromString(databaseHostAndPort);
        } else {
            return null;
        }
    }

    @Override
    public String getDatabaseHost() {
        HostAndPort endpoint = getDatabaseHostAndPort();
        if (endpoint != null) {
            return endpoint.getHostText();
        } else {
            return getEntity().getConfig(CampsiteConfig.DATABASE_HOST);
        }
    }

    @Override
    public Integer getDatabasePort() {
        HostAndPort endpoint = getDatabaseHostAndPort();
        if (endpoint != null) {
            return endpoint.getPort();
        } else {
            return getEntity().getConfig(CampsiteConfig.DATABASE_PORT);
        }
    }

    @Override
    public String getDatabaseUser() {
        return getEntity().getConfig(CampsiteConfig.DATABASE_USER);
    }

    @Override
    public String getDatabasePassword() {
        return getEntity().getConfig(CampsiteConfig.DATABASE_PASSWORD);
    }

    @Override
    public String getMailerUser() {
        return getEntity().getConfig(CampsiteConfig.MAILER_USER);
    }

    @Override
    public String getMailerPassword() {
        return getEntity().getConfig(CampsiteConfig.MAILER_PASSWORD);
    }

    @Override
    public String getRecaptchaPublicKey() {
        return getEntity().getConfig(CampsiteConfig.RECAPTCHA_PUBLIC_KEY);
    }

    @Override
    public String getRecaptchaPrivateKey() {
        return getEntity().getConfig(CampsiteConfig.RECAPTCHA_PRIVATE_KEY);
    }

    @Override
    public String getObjectStorage() {
        return getEntity().getConfig(CampsiteConfig.OBJECT_STORAGE);
    }

    @Override
    public String getS3AccessKey() {
        return getEntity().getConfig(CampsiteConfig.S3_ACCESS_KEY);
    }

    @Override
    public String getS3SecretKey() {
        return getEntity().getConfig(CampsiteConfig.S3_SECRET_KEY);
    }

    @Override
    public String getS3BucketName() {
        return getEntity().getConfig(CampsiteConfig.S3_BUCKET_NAME);
    }

    @Override
    public String getS3PrivateBucketName() {
        return getEntity().getConfig(CampsiteConfig.S3_PRIVATE_BUCKET_NAME);
    }

    @Override
    public String getHpcloudAccessKey() {
        return getEntity().getConfig(CampsiteConfig.HP_CLOUD_ACCESS_KEY);
    }

    @Override
    public String getHpcloudSecretKey() {
        return getEntity().getConfig(CampsiteConfig.HP_CLOUD_SECRET_KEY);
    }

    @Override
    public String getHpcloudTenantId() {
        return getEntity().getConfig(CampsiteConfig.HP_CLOUD_TENANT_ID);
    }

    @Override
    public String getEmailService() {
        return getEntity().getConfig(CampsiteConfig.EMAIL_SERVICE);
    }

    @Override
    public String getSendgridUsername() {
        return getEntity().getConfig(CampsiteConfig.SENDGRID_USER);
    }

    @Override
    public String getSendgridPassword() {
        return getEntity().getConfig(CampsiteConfig.SENDGRID_PASSWORD);
    }

    @Override
    public String getQueueService() {
        return getEntity().getConfig(CampsiteConfig.QUEUE_SERVICE);
    }

    @Override
    public HostAndPort getRabbitHostAndPort() {
        String rabbitHostAndPort = getEntity().getConfig(CampsiteConfig.RABBIT_HOST_AND_PORT);
        if (Strings.isNonBlank(rabbitHostAndPort)) {
            return HostAndPort.fromString(rabbitHostAndPort);
        } else {
            return null;
        }
    }

    @Override
    public String getRabbitHost() {
        HostAndPort endpoint = getRabbitHostAndPort();
        if (endpoint != null) {
            return endpoint.getHostText();
        } else {
            return getEntity().getConfig(CampsiteConfig.RABBIT_HOST);
        }
    }

    @Override
    public Integer getRabbitPort() {
        HostAndPort endpoint = getRabbitHostAndPort();
        if (endpoint != null) {
            return endpoint.getPort();
        } else {
            return getEntity().getConfig(CampsiteConfig.RABBIT_PORT);
        }
    }

    @Override
    public String getRabbitUser() {
        return getEntity().getConfig(CampsiteConfig.RABBIT_USER);
    }

    @Override
    public String getRabbitPassword() {
        return getEntity().getConfig(CampsiteConfig.RABBIT_PASSWORD);
    }

    @Override
    public HostAndPort getMemcachedHostAndPort() {
        String memcachedHostAndPort = getEntity().getConfig(CampsiteConfig.MEMCACHED_HOST_AND_PORT);
        if (Strings.isNonBlank(memcachedHostAndPort)) {
            return HostAndPort.fromString(memcachedHostAndPort);
        } else {
            return null;
        }
    }

    @Override
    public String getMemcachedHost() {
        HostAndPort endpoint = getMemcachedHostAndPort();
        if (endpoint != null) {
            return endpoint.getHostText();
        } else {
            return getEntity().getConfig(CampsiteConfig.MEMCACHED_HOST);
        }
    }

    @Override
    public Integer getMemcachedPort() {
        HostAndPort endpoint = getMemcachedHostAndPort();
        if (endpoint != null) {
            return endpoint.getPort();
        } else {
            return getEntity().getConfig(CampsiteConfig.MEMCACHED_PORT);
        }
    }

}
