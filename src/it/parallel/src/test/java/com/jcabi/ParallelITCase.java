/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi;

import com.jcabi.jdbc.JdbcSession;
import java.sql.Connection;
import java.sql.DriverManager;
import org.junit.Test;

/**
 * Test case for {@link Parallel}.
 * @since 1.0
 */
public final class ParallelITCase {

    /**
     * First MySQL port.
     */
    private static final String FIRST =
        System.getProperty("failsafe.mysql.first");

    /**
     * MySQL works.
     * @throws Exception If something is wrong
     */
    @Test
    public void basicMySqlManipulations() throws Exception {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        this.process(Integer.parseInt(ParallelITCase.FIRST));
    }

    /**
     * Process on this port.
     * @param port Port to process
     * @throws Exception If fails
     */
    private void process(final int port) throws Exception {
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
    }

}
