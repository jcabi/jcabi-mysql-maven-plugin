/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.mysql.maven.plugin;

import com.jcabi.aspects.Cacheable;
import com.jcabi.log.Logger;
import java.io.File;
import java.util.Collections;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * Abstract MOJO.
 * @since 0.1
 */
@ToString
@EqualsAndHashCode(callSuper = false)
abstract class AbstractMysqlMojo extends AbstractMojo {

    /**
     * Property that will be exported by the plugin indicating if an existing
     * database could be reused.
     */
    private static final String PROPERTY_REUSED = "jcabi.reused.database";

    /**
     * The Maven project.
     */
    @Parameter(
        defaultValue = "${project}",
        readonly = true
    )
    private transient MavenProject project;

    /**
     * Shall we skip execution?
     */
    @Parameter(
        defaultValue = "false",
        required = false
    )
    private transient boolean skip;

    /**
     * Port to use.
     */
    @Parameter(
        defaultValue = "3306",
        required = false
    )
    private transient int port;

    /**
     * Location of MySQL distribution.
     */
    @Parameter(
        defaultValue = "${project.build.directory}/mysql-dist",
        required = true
    )
    private transient File dist;

    /**
     * Username to use.
     */
    @Parameter(
        defaultValue = "root",
        required = false
    )
    private transient String user;

    /**
     * Password to use.
     */
    @Parameter(
        defaultValue = "root",
        required = false
    )
    private transient String password;

    /**
     * Database name to use.
     */
    @Parameter(
        defaultValue = "root",
        required = false
    )
    private transient String dbname;

    /**
     * Location of MySQL data.
     */
    @Parameter(
        defaultValue = "${project.build.directory}/mysql-data",
        required = true
    )
    private transient File data;

    /**
     * Override location of MySQL socket file.  Defaults to
     * (data dir)/mysql.socket.
     */
    @Parameter(required = false)
    private transient File socket;

    /**
     * Shall we always delete an existing database or reuse it?
     */
    @Parameter(
        defaultValue = "true",
        required = false
    )
    private transient boolean erase;

    /**
     * Configuration options.
     */
    @Parameter(
        required = false
    )
    private transient List<String> options;

    /**
     * Set skip option.
     * @param skp Shall we skip execution?
     */
    public void setSkip(final boolean skp) {
        this.skip = skp;
    }

    @Override
    public void execute() throws MojoFailureException {
        StaticLoggerBinder.getSingleton().setMavenLog(this.getLog());
        if (this.skip) {
            Logger.info(this, "execution skipped because of 'skip' option");
            return;
        }
        this.run(AbstractMysqlMojo.instances());
        if (this.project == null) {
            Logger.warn(
                this,
                "MavenProject not initialized, unable to set property %s",
                AbstractMysqlMojo.PROPERTY_REUSED
            );
        } else {
            Logger.info(
                this,
                "set Maven property %s = %s ",
                AbstractMysqlMojo.PROPERTY_REUSED,
                AbstractMysqlMojo.instances().reusedExistingDatabase()
            );
            this.project.getProperties().setProperty(
                AbstractMysqlMojo.PROPERTY_REUSED,
                Boolean.toString(
                    AbstractMysqlMojo.instances().reusedExistingDatabase()
                )
            );
        }
    }

    /**
     * Get directory with MySQL dist.
     * @return Directory
     * @throws MojoFailureException If fails
     */
    public File distDir() throws MojoFailureException {
        if (!this.dist.exists()) {
            throw new MojoFailureException(
                String.format(
                    "MySQL distribution directory doesn't exist: %s", this.dist
                )
            );
        }
        return this.dist;
    }

    /**
     * Get directory with MySQL data.
     * @return Directory
     */
    public File dataDir() {
        return this.data;
    }

    /**
     * Get MySQL socket location.
     * @return Overridden socket location (null for default)
     */
    public File socketFile() {
        return this.socket;
    }

    /**
     * If true, always delete existing database files and create a new instance
     * from scratch. If false, try to reuse existing files.
     * @return If existing database files should be deleted.
     */
    public boolean clear() {
        return this.erase;
    }

    /**
     * Get configuration.
     * @return Configuration
     */
    public Config config() {
        if (this.options == null) {
            this.options = Collections.emptyList();
        }
        return new Config(
            this.port, this.user, this.password, this.dbname,
            Collections.unmodifiableList(this.options)
        );
    }

    /**
     * Run custom functionality.
     * @param instances Instances to work with
     * @throws MojoFailureException If fails
     */
    protected abstract void run(Instances instances) throws MojoFailureException;

    /**
     * Get instances.
     * @return Instances
     */
    @Cacheable(forever = true)
    private static Instances instances() {
        return new Instances();
    }

}
