/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi;

import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.UrlSource;
import java.sql.Connection;
import java.sql.DriverManager;
import javax.sql.DataSource;
import org.junit.Test;

/**
 * Test case for {@link Foo}.
 * @since 1.0
 */
public final class WithOptionsITCase {

    /**
     * MySQL port.
     */
    private static final String PORT =
        System.getProperty("failsafe.mysql.port");

    /**
     * Can use configuration options.
     * @throws Exception If something is wrong
     */
    @Test
    public void canReceiveConfigurationOptions() throws Exception {
        final DataSource source = new UrlSource(
            String.format(
                "jdbc:mysql://localhost:%s/root?user=root&password=root",
                WithOptionsITCase.PORT
            )
        );
        new JdbcSession(source)
            .autocommit(false)
            .sql("CREATE TABLE foo (date DATE)")
            .execute()
            .sql("INSERT INTO foo VALUES ('2004-04-31')")
            .execute()
            .sql("SELECT * FROM foo")
            .execute()
            .sql("DROP TABLE foo")
            .execute();
    }

}
