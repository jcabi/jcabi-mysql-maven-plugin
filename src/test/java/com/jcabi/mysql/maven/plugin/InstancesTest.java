/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.mysql.maven.plugin;

import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.SingleOutcome;
import com.jcabi.jdbc.UrlSource;
import java.io.File;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link Instances}.
 * @since 0.6
 */
final class InstancesTest {

    /**
     * User.
     */
    static final String USER = "u13";

    /**
     * Password.
     */
    static final String PASSWORD = "swordfish";

    /**
     * Database name.
     */
    static final String DBNAME = "papamama";

    /**
     * Time to sleep in between instances.
     */
    private static final long SLEEP_SECONDS = 5L;

    /**
     * Location of MySQL dist.
     */
    private static final String DIST = getDist();

    /**
     * MySQL connection string format.
     */
    private static final String CONNECTION_STRING =
        "jdbc:mysql://localhost:%d/%s?user=%s&password=%s";

    /**
     * Instances can start and stop.
     * @throws Exception If something is wrong
     */
    @Test
    void startsAndStops() throws Exception {
        final int port = this.reserve();
        final Instances instances = new Instances();
        instances.start(
            new Config(
                port,
                InstancesTest.USER,
                InstancesTest.PASSWORD,
                InstancesTest.DBNAME,
                Collections.emptyList()
            ),
            new File(InstancesTest.DIST),
            Files.createTempDirectory("").toFile(),
            true,
            null
        );
        try {
            MatcherAssert.assertThat(
                "the inserted row cannot go missing",
                new JdbcSession(
                    new UrlSource(
                        String.format(
                            InstancesTest.CONNECTION_STRING,
                            port,
                            InstancesTest.DBNAME,
                            InstancesTest.USER,
                            InstancesTest.PASSWORD
                        )
                    )
                ).autocommit(false)
                    .sql("CREATE TABLE foo (id INT)")
                    .execute()
                    .sql("INSERT INTO foo VALUES (1)")
                    .execute()
                    .sql("SELECT COUNT(*) FROM foo")
                    .select(new SingleOutcome<>(Long.class)),
                Matchers.equalTo(1L)
            );
        } finally {
            instances.stop(port);
        }
    }

    /**
     * Instances can use option.
     * Test creates and inserts incorrect date in it
     * Without option "--sql-mode=ALLOW_INVALID_DATES" it produces
     * invalid date error.
     * @throws Exception If something is wrong
     */
    @Test
    void useOptions() throws Exception {
        final int port = this.reserve();
        final Instances instances = new Instances();
        instances.start(
            new Config(
                port,
                InstancesTest.USER,
                InstancesTest.PASSWORD,
                InstancesTest.DBNAME,
                Collections.singletonList("sql-mode=ALLOW_INVALID_DATES")
            ),
            new File(InstancesTest.DIST),
            Files.createTempDirectory("").toFile(),
            true,
            null
        );
        try {
            MatcherAssert.assertThat(
                "the invalid date cannot be rejected",
                new JdbcSession(
                    new UrlSource(
                        String.format(
                            InstancesTest.CONNECTION_STRING,
                            port,
                            InstancesTest.DBNAME,
                            InstancesTest.USER,
                            InstancesTest.PASSWORD
                        )
                    )
                ).autocommit(false)
                    .sql("CREATE TABLE foo (date DATE)")
                    .execute()
                    .sql("INSERT INTO foo VALUES ('2004-04-31')")
                    .execute()
                    .sql("SELECT COUNT(*) FROM foo")
                    .select(new SingleOutcome<>(Long.class)),
                Matchers.equalTo(1L)
            );
        } finally {
            instances.stop(port);
        }
    }

    /**
     * Instances can use custom db user name.
     * @throws Exception If something is wrong
     * @todo #8 Create integration tests for Config.
     *  Integration tests 'WithConfigITCase' should be created to test
     *  that user name, password and dbname are set properly.
     *  This issue should be done after non root user name is set properly
     */
    @Test
    void canUseCustomDbUserName() throws Exception {
        final int port = this.reserve();
        final String user = "notRoot";
        final Instances instances = new Instances();
        instances.start(
            new Config(
                port,
                user,
                InstancesTest.PASSWORD,
                InstancesTest.DBNAME,
                Collections.<String>emptyList()
            ),
            new File(InstancesTest.DIST),
            Files.createTempDirectory("").toFile(),
            true,
            null
        );
        try {
            MatcherAssert.assertThat(
                "the custom user cannot be refused by the database",
                new JdbcSession(
                    new UrlSource(
                        String.format(
                            InstancesTest.CONNECTION_STRING,
                            port,
                            InstancesTest.DBNAME,
                            user,
                            InstancesTest.PASSWORD
                        )
                    )
                ).autocommit(false)
                    .sql("CREATE TABLE foo (id INT)")
                    .execute()
                    .sql("INSERT INTO foo VALUES (1)")
                    .execute()
                    .sql("SELECT COUNT(*) FROM foo")
                    .select(new SingleOutcome<>(Long.class)),
                Matchers.equalTo(1L)
            );
        } finally {
            instances.stop(port);
        }
    }

    /**
     * Instances can use custom db password.
     * Password changed with username, because we don't support
     * changing password for existing user
     * @throws Exception If something is wrong
     */
    @Test
    void canUseCustomDbPassword() throws Exception {
        final int port = this.reserve();
        final String user = "notRoot";
        final String password = "notRoot";
        final Instances instances = new Instances();
        instances.start(
            new Config(
                port,
                user,
                password,
                InstancesTest.DBNAME,
                Collections.<String>emptyList()
            ),
            new File(InstancesTest.DIST),
            Files.createTempDirectory("").toFile(),
            true,
            null
        );
        try {
            MatcherAssert.assertThat(
                "the custom password cannot be refused by the database",
                new JdbcSession(
                    new UrlSource(
                        String.format(
                            InstancesTest.CONNECTION_STRING,
                            port,
                            InstancesTest.DBNAME,
                            user,
                            password
                        )
                    )
                ).autocommit(false)
                    .sql("CREATE TABLE foo (id INT)")
                    .execute()
                    .sql("INSERT INTO foo VALUES (1)")
                    .execute()
                    .sql("SELECT COUNT(*) FROM foo")
                    .select(new SingleOutcome<>(Long.class)),
                Matchers.equalTo(1L)
            );
        } finally {
            instances.stop(port);
        }
    }

    /**
     * Instances can use custom db name.
     * @throws Exception If something is wrong
     */
    @Test
    void canUseCustomDbDbName() throws Exception {
        final int port = this.reserve();
        final String dbname = "notRoot";
        final Instances instances = new Instances();
        instances.start(
            new Config(
                port,
                InstancesTest.USER,
                InstancesTest.PASSWORD,
                dbname,
                Collections.<String>emptyList()
            ),
            new File(InstancesTest.DIST),
            Files.createTempDirectory("").toFile(),
            true,
            null
        );
        try {
            MatcherAssert.assertThat(
                "the custom database name cannot be ignored",
                new JdbcSession(
                    new UrlSource(
                        String.format(
                            InstancesTest.CONNECTION_STRING,
                            port,
                            dbname,
                            InstancesTest.USER,
                            InstancesTest.PASSWORD
                        )
                    )
                ).autocommit(false)
                    .sql("CREATE TABLE foo (id INT)")
                    .execute()
                    .sql("INSERT INTO foo VALUES (1)")
                    .execute()
                    .sql("SELECT COUNT(*) FROM foo")
                    .select(new SingleOutcome<>(Long.class)),
                Matchers.equalTo(1L)
            );
        } finally {
            instances.stop(port);
        }
    }

    /**
     * If no database exists, it will create one even if clear = false.
     * @throws Exception If something is wrong
     */
    @Test
    void willCreateDatabaseEvenWithoutClear() throws Exception {
        final int port = this.reserve();
        final Instances instances = new Instances();
        instances.start(
            new Config(
                port,
                InstancesTest.USER,
                InstancesTest.PASSWORD,
                InstancesTest.DBNAME,
                Collections.emptyList()
            ),
            new File(InstancesTest.DIST),
            Files.createTempDirectory("").toFile(),
            false,
            null
        );
        try {
            new JdbcSession(
                new UrlSource(
                    String.format(
                        InstancesTest.CONNECTION_STRING,
                        port,
                        InstancesTest.DBNAME,
                        InstancesTest.USER,
                        InstancesTest.PASSWORD
                    )
                )
            ).autocommit(false)
                .sql("CREATE TABLE foo (id INT)")
                .execute()
                .sql("INSERT INTO foo VALUES (1)")
                .execute()
                .sql("DROP TABLE foo")
                .execute();
            MatcherAssert.assertThat(
                "a missing database cannot be reported as reused",
                !instances.reusedExistingDatabase()
            );
        } finally {
            instances.stop(port);
        }
    }

    /**
     * Is able to reuse a previously created database.
     * @throws Exception If something is wrong
     */
    @Test
    @Disabled
    void canReuseExistingDatabase() throws Exception {
        final int port = this.reserve();
        final File target = Files.createTempDirectory("").toFile();
        final Instances instances = new Instances();
        instances.start(
            new Config(
                port,
                InstancesTest.USER,
                InstancesTest.PASSWORD,
                InstancesTest.DBNAME,
                Collections.emptyList()
            ),
            new File(InstancesTest.DIST),
            target,
            true,
            null
        );
        try {
            MatcherAssert.assertThat(
                "the committed row cannot go missing",
                new JdbcSession(
                    new UrlSource(
                        String.format(
                            InstancesTest.CONNECTION_STRING,
                            port,
                            InstancesTest.DBNAME,
                            InstancesTest.USER,
                            InstancesTest.PASSWORD
                        )
                    )
                ).autocommit(false)
                    .sql("START TRANSACTION")
                    .execute()
                    .sql("CREATE TABLE foo (id INT)")
                    .execute()
                    .sql("INSERT INTO foo VALUES (1)")
                    .execute()
                    .sql("COMMIT")
                    .execute()
                    .sql("SELECT COUNT(*) FROM foo")
                    .select(new SingleOutcome<>(Long.class)),
                Matchers.equalTo(1L)
            );
        } finally {
            instances.stop(port);
        }
        this.reuse(target);
    }

    private void reuse(final File target) throws Exception {
        final File socket = new File(target, "mysql.sock");
        while (socket.exists()) {
            TimeUnit.SECONDS.sleep(InstancesTest.SLEEP_SECONDS);
        }
        final int port = this.reserve();
        final Instances instances = new Instances();
        instances.start(
            new Config(
                port,
                InstancesTest.USER,
                InstancesTest.PASSWORD,
                InstancesTest.DBNAME,
                Collections.emptyList()
            ),
            new File(InstancesTest.DIST),
            target,
            false,
            null
        );
        do {
            TimeUnit.SECONDS.sleep(InstancesTest.SLEEP_SECONDS);
        } while (!socket.exists());
        try {
            MatcherAssert.assertThat(
                "the row committed by the previous instance cannot be lost",
                new JdbcSession(
                    new UrlSource(
                        String.format(
                            InstancesTest.CONNECTION_STRING,
                            port,
                            InstancesTest.DBNAME,
                            InstancesTest.USER,
                            InstancesTest.PASSWORD
                        )
                    )
                ).autocommit(false)
                    .sql("SELECT COUNT(*) FROM foo")
                    .select(new SingleOutcome<>(Long.class)),
                Matchers.equalTo(1L)
            );
        } finally {
            instances.stop(port);
        }
    }

    private int reserve() throws Exception {
        final int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        return port;
    }

    private static String getDist() {
        String dist = System.getProperty("surefire.dist");
        if (dist == null) {
            dist = "./target/mysql-dist";
        }
        return dist;
    }
}
