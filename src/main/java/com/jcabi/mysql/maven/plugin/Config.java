/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.mysql.maven.plugin;

import java.util.Collections;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Configuration POJO.
 *
 * <p>Contains configuration for particular db instance {@link Instances}.</p>
 *
 * @since 0.6
 */
@ToString
@EqualsAndHashCode(
    of = { "tcpport", "dbuser", "dbpassword", "name", "dbopts" }
)
public final class Config {

    /**
     * TCP port.
     */
    private final transient int tcpport;

    /**
     * Db user name.
     */
    private final transient String dbuser;

    /**
     * Db password.
     */
    private final transient String dbpassword;

    /**
     * Db name.
     */
    private final transient String name;

    /**
     * Configuration options.
     */
    private final transient List<String> dbopts;

    /**
     * Creates new configuration.
     * @param port TCP port
     * @param usr Db user name
     * @param password Db password
     * @param dbn Db name
     * @param opts Configuration options
     * @checkstyle ParameterNumberCheck (15 lines)
     */
    public Config(
        final int port,
        @NotNull final String usr,
        @NotNull final String password,
        @NotNull final String dbn,
        @NotNull final List<String> opts
    ) {
        this.tcpport = port;
        this.dbuser = usr;
        this.dbpassword = password;
        this.name = dbn;
        this.dbopts = Collections.unmodifiableList(opts);
    }

    /**
     * Get TCP port we're on.
     * @return Port number
     */
    public int port() {
        return this.tcpport;
    }

    /**
     * Get Db user name.
     * @return User name
     */
    public String user() {
        return this.dbuser;
    }

    /**
     * Get Db password.
     * @return Password
     */
    public String password() {
        return this.dbpassword;
    }

    /**
     * Get Db name.
     * @return Database name
     */
    public String dbname() {
        return this.name;
    }

    /**
     * Get configuration options.
     * @return Options
     */
    public List<String> options() {
        return this.dbopts;
    }
}
