/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.mysql.maven.plugin;

import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.UrlSource;
import java.io.File;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link Instances}.
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 * @checkstyle MultipleStringLiterals (500 lines)
 * @since 0.6
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class InstancesTest {

    /**
     * User.
     */
    public static final String USER = "u13";

    /**
     * Password.
     */
    public static final String PASSWORD = "swordfish";

    /**
     * Database name.
     */
    public static final String DBNAME = "papamama";

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
        final DataSource source = new UrlSource(
            String.format(
                InstancesTest.CONNECTION_STRING,
                port,
                InstancesTest.DBNAME,
                InstancesTest.USER,
                InstancesTest.PASSWORD
            )
        );
        try {
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
            final DataSource source = new UrlSource(
                String.format(
                    InstancesTest.CONNECTION_STRING,
                    port,
                    InstancesTest.DBNAME,
                    InstancesTest.USER,
                    InstancesTest.PASSWORD
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
        final DataSource source = new UrlSource(
            String.format(
                InstancesTest.CONNECTION_STRING,
                port,
                InstancesTest.DBNAME,
                user,
                InstancesTest.PASSWORD
            )
        );
        try {
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
        final DataSource source = new UrlSource(
            String.format(
                InstancesTest.CONNECTION_STRING,
                port,
                InstancesTest.DBNAME,
                user,
                password
            )
        );
        try {
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
        final DataSource source = new UrlSource(
            String.format(
                InstancesTest.CONNECTION_STRING,
                port,
                dbname,
                InstancesTest.USER,
                InstancesTest.PASSWORD
            )
        );
        try {
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
        MatcherAssert.assertThat(
            "Instance reusedExistingDatabase should be false.",
            !instances.reusedExistingDatabase()
        );
        final DataSource source = new UrlSource(
            String.format(
                InstancesTest.CONNECTION_STRING,
                port,
                InstancesTest.DBNAME,
                InstancesTest.USER,
                InstancesTest.PASSWORD
            )
        );
        try {
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
    void canReuseExistingDatabse() throws Exception {
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
        MatcherAssert.assertThat(
            "Instance reusedExistingDatabase should be false.",
            !instances.reusedExistingDatabase()
        );
        final DataSource source = new UrlSource(
            String.format(
                InstancesTest.CONNECTION_STRING,
                port,
                InstancesTest.DBNAME,
                InstancesTest.USER,
                InstancesTest.PASSWORD
            )
        );
        try {
            new JdbcSession(source)
                .autocommit(false)
                .sql("START TRANSACTION")
                .execute()
                .sql("CREATE TABLE foo (id INT)")
                .execute()
                .sql("INSERT INTO foo VALUES (1)")
                .execute()
                .sql("SELECT COUNT(*) FROM foo")
                .execute()
                .sql("COMMIT")
                .execute();
        } finally {
            instances.stop(port);
        }
        this.checkExistingDatabase(target);
    }

    /**
     * Helper for canReuseExistingDatabse test.
     * @param target Directory of existing database
     * @throws Exception If something is wrong
     */
    private void checkExistingDatabase(final File target) throws Exception {
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
        MatcherAssert.assertThat(
            "Instance reusedExistingDatabase should be true.",
            instances.reusedExistingDatabase()
        );
        do {
            TimeUnit.SECONDS.sleep(InstancesTest.SLEEP_SECONDS);
        } while (!socket.exists());
        try {
            final DataSource source = new UrlSource(
                String.format(
                    InstancesTest.CONNECTION_STRING,
                    port,
                    InstancesTest.DBNAME,
                    InstancesTest.USER,
                    InstancesTest.PASSWORD
                )
            );
            new JdbcSession(source)
                .autocommit(false)
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
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
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
