/**
 * Copyright (c) 2012-2017, jcabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
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
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
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
    protected abstract void run(final Instances instances)
        throws MojoFailureException;

    /**
     * Get instances.
     * @return Instances
     */
    @Cacheable(forever = true)
    private static Instances instances() {
        return new Instances();
    }

}
