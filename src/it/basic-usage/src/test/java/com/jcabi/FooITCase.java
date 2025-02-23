/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi;

import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.UrlSource;
import java.sql.Connection;
import java.sql.DriverManager;
import javax.sql.DataSource;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Foo}.
 * @since 1.0
 */
public final class FooITCase {

    /**
     * MySQL port.
     */
    private static final String PORT =
        System.getProperty("failsafe.mysql.port");

    /**
     * It is not a default MySQL port.
     * @throws Exception If something is wrong
     */
    @Test
    public void itIsCustomMySqlPort() throws Exception {
        MatcherAssert.assertThat(
            FooITCase.PORT,
            Matchers.not(Matchers.equalTo("3306"))
        );
    }

    /**
     * MySQL works.
     * @throws Exception If something is wrong
     */
    @Test
    public void basicMySqlManipulations() throws Exception {
        final DataSource source = new UrlSource(
            String.format(
                "jdbc:mysql://localhost:%s/root?user=root&password=root",
                FooITCase.PORT
            )
        );
        new JdbcSession(source)
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
