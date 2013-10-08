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

import com.google.common.io.Files;
import com.jcabi.jdbc.JdbcSession;
import java.io.File;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import org.junit.Test;

/**
 * Test case for {@link Instances}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class InstancesTest {

    /**
     * Location of MySQL dist.
     */
    private static final String DIST = System.getProperty("surefire.dist");

    /**
     * Instances can start and stop.
     * @throws Exception If something is wrong
     */
    @Test
    public void startsAndStops() throws Exception {
        final int port = this.reserve();
        final Instances instances = new Instances();
        instances.start(
            port, new File(InstancesTest.DIST),
            Files.createTempDir()
        );
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        try {
            final Connection conn = DriverManager.getConnection(
                String.format(
                    "jdbc:mysql://localhost:%d/root?user=root&password=root",
                    port
                )
            );
            new JdbcSession(conn)
                .autocommit(false)
                .sql("CREATE TABLE foo (id INT)")
                .execute()
                .sql("INSERT INTO foo VALUES (1)")
                .execute()
                .sql("SELECT COUNT(*) FROM foo")
                .execute()
                .sql("DROP TABLE foo")
                .execute();
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

}
