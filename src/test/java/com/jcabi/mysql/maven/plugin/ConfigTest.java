/**
 * Copyright (c) 2012-2014, jcabi.com
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

import com.google.common.io.Files;
import com.jcabi.jdbc.JdbcSession;
import java.io.File;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Collections;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test case for {@link Config}.
 * todo: #63 This test and other tests which use MySQL
 * do fail when Travis build runs. When #63 will be fixed
 * <code>@Ignore</code> must be removed from this test and
 * other with todo #63
 * @author Alexander Paderin (apocarteres@gmail.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (170 lines)
 * @checkstyle MultipleStringLiterals (170 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@Ignore
public final class ConfigTest {

    /**
     * User.
     */
    public static final String USER = "user";

    /**
     * Password.
     */
    public static final String PASSWORD = "userPass";

    /**
     * Database name.
     */
    public static final String DBNAME = "userDb";

    /**
     * Location of MySQL dist.
     */
    private static final String DIST = getDist();

    /**
     * MySQL driver name.
     */
    private static final String DRIVER = "com.mysql.jdbc.Driver";

    /**
     * MySQL connection string format.
     */
    private static final String CONNECTION_STRING =
        "jdbc:mysql://localhost:%d/%s?user=%s&password=%s";

    /**
     * Driver returns connection that belongs to specified non-root user.
     * @throws Exception If something is wrong
     */
    @Test
    public void connectionBelongsToUserFromConfig() throws Exception {
        final int port = this.reserve();
        final Instances instances = new Instances();
        instances.start(
            new Config(
                port,
                ConfigTest.USER,
                ConfigTest.PASSWORD,
                ConfigTest.DBNAME,
                Collections.<String>emptyList()
            ),
            new File(ConfigTest.DIST),
            Files.createTempDir(),
            true,
            null
        );
        Class.forName(ConfigTest.DRIVER).newInstance();
        try {
            final Connection conn = DriverManager.getConnection(
                String.format(
                    ConfigTest.CONNECTION_STRING,
                    port,
                    ConfigTest.DBNAME,
                    ConfigTest.USER,
                    ConfigTest.PASSWORD
                )
            );
            final String url = conn.getMetaData().getURL();
            try {
                new JdbcSession(conn)
                    .autocommit(false)
                    .sql("SHOW TABLES")
                    .execute();
            } finally {
                conn.close();
            }
            MatcherAssert.assertThat(
                String.format(
                    CONNECTION_STRING, port, DBNAME, USER, PASSWORD
                ),
                Matchers.is(url)
            );
        } finally {
            instances.stop(port);
        }
    }

    /**
     * Find and return the first available port.
     * @return The port number
     * @throws Exception If fails
     */
    private int reserve() throws Exception {
        final ServerSocket socket = new ServerSocket(0);
        try {
            return socket.getLocalPort();
        } finally {
            socket.close();
        }
    }

    /**
     * This will be taken from the surefire.dist system property
     * or defaulted to the target.
     * @return The MySQL distribution location
     */
    private static String getDist() {
        String dist = System.getProperty("surefire.dist");
        if (dist == null) {
            dist = "./target/mysql-dist";
        }
        return dist;
    }
}
