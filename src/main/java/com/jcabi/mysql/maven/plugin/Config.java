/**
 * Copyright (c) 2012-2013, JCabi.com
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

import java.util.Collections;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Configuration POJO.
 * Contains configuration for particular db instance {@link Instances}
 *
 * @author Paul Polischuk (ppol@ua.fm)
 * @version $Id$
 * @since 0.6
 */
@ToString
@EqualsAndHashCode(
    of = { "tcpport", "dbuser", "dbpassword", "dbname", "options" }
)
public final class Config {

    /**
     * TCP port.
     */
    private final int tcpport;

    /**
     * Db user name.
     */
    private final String dbuser;

    /**
     * Db password.
     */
    private final String dbpassword;

    /**
     * Db name.
     */
    private final String dbname;

    /**
     * Configuration options.
     */
    private final List<String> options;

    /**
     * Creates new configuration.
     * @param port TCP port
     * @param user Db user name
     * @param password Db password
     * @param dbn Db name
     * @param opts Configuration options
     */
    public Config(
        final int port,
        @NotNull final String user,
        @NotNull final String password,
        @NotNull final String dbn,
        @NotNull final List<String> opts
    ) {
        this.tcpport = port;
        this.dbuser = user;
        this.dbpassword = password;
        this.dbname = dbn;
        this.options = Collections.unmodifiableList(opts);
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
        return this.dbname;
    }

    /**
     * Get configuration options.
     * @return Options
     */
    public List<String> options() {
        return this.options;
    }
}
