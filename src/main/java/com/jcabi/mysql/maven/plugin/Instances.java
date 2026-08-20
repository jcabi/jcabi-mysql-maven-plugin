/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.mysql.maven.plugin;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseProcess;
import com.jcabi.log.VerboseRunnable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Running instances of MySQL.
 *
 * <p>The class is thread-safe.
 *
 * @since 0.1
 */
@ToString
@EqualsAndHashCode(of = "processes")
@Loggable(Loggable.INFO)
public final class Instances {

    /**
     * Directory of the actual database relative to the target.
     */
    private static final String DATA_SUB_DIR = "data";

    /**
     * No defaults.
     */
    private static final String NO_DEFAULTS = "--no-defaults";

    /**
     * Default retry count.
     */
    private static final int RETRY_COUNT = 5;

    /**
     * Default user.
     */
    private static final String DEFAULT_USER = "root";

    /**
     * Default password.
     */
    private static final String DEFAULT_PASSWORD = "root";

    /**
     * Default host.
     */
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    private static final String DEFAULT_HOST = "127.0.0.1";

    /**
     * Running processes.
     */
    private final transient ConcurrentMap<Integer, Process> processes;

    /**
     * Guard of the running processes.
     */
    private final transient Lock lock;

    /**
     * If true, always create a new database. If false, check if there is an
     * existing database at the target location and try to use that if
     * possible, otherwise create a new one anyway.
     */
    private transient boolean clean;

    /**
     * Public ctor.
     */
    public Instances() {
        this.processes = new ConcurrentHashMap<>(0);
        this.lock = new ReentrantLock();
        this.clean = true;
    }

    /**
     * Start a new one at this port.
     * @param config Instance configuration
     * @param dist Path to MySQL distribution
     * @param target Where to keep temp data
     * @param deldir If existing DB should be deleted
     * @param socket Alternative socket location for mysql (may be null)
     * @throws IOException If fails to start
     * @checkstyle ParameterNumberCheck (10 lines)
     */
    public void start(@NotNull final Config config, @NotNull final File dist,
        @NotNull final File target, final boolean deldir, final File socket)
        throws IOException {
        this.setClean(target, deldir);
        this.lock.lock();
        try {
            if (this.processes.containsKey(config.port())) {
                throw new IllegalArgumentException(
                    String.format("Port %d is already busy", config.port())
                );
            }
            this.processes.put(
                config.port(),
                this.process(config, dist, target, socket)
            );
            Runtime.getRuntime().addShutdownHook(
                new Thread(() -> this.stop(config.port()))
            );
        } finally {
            this.lock.unlock();
        }
        Logger.info(
            this,
            "MySQL database is up and running at the %d port",
            config.port()
        );
    }

    /**
     * Stop a running one at this port.
     * @param port The port to stop at
     */
    public void stop(final int port) {
        this.lock.lock();
        try {
            final Process proc = this.processes.remove(port);
            if (proc != null) {
                proc.destroy();
            }
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Returns if a clean database had to be created. Note that this must be
     * called after {@link Instances#start(Config, File, File, boolean, File)}.
     * @return If this is a clean database or could have been reused
     */
    public boolean reusedExistingDatabase() {
        return !this.clean;
    }

    private Process process(@NotNull final Config config,
        final File dist, final File target, final File socketfile)
        throws IOException {
        this.prepareFolders(target);
        final File socket;
        if (socketfile == null) {
            socket = new File(target, "mysql.sock");
        } else {
            socket = socketfile;
        }
        final ProcessBuilder builder = this.builder(
            dist,
            "bin/mysqld",
            Instances.NO_DEFAULTS,
            String.format("--user=%s", System.getProperty("user.name")),
            "--general_log",
            "--console",
            "--innodb_buffer_pool_size=64M",
            "--innodb_log_file_size=64M",
            "--innodb_use_native_aio=0",
            String.format("--binlog-ignore-db=%s", config.dbname()),
            String.format("--basedir=%s", dist),
            String.format("--lc-messages-dir=%s", new File(dist, "share")),
            String.format("--datadir=%s", this.data(dist, target)),
            String.format("--tmpdir=%s", new File(target, "temp")),
            String.format("--socket=%s", socket),
            String.format("--log-error=%s", new File(target, "errors.log")),
            String.format("--general-log-file=%s", new File(target, "mysql.log")),
            String.format("--pid-file=%s", new File(target, "mysql.pid")),
            String.format("--port=%d", config.port())
        ).redirectErrorStream(true);
        builder.environment().put("MYSQL_HOME", dist.getAbsolutePath());
        for (final String option : config.options()) {
            if (!StringUtils.isBlank(option)) {
                builder.command().add(String.format("--%s", option));
            }
        }
        final Process proc = builder.start();
        final Thread thread = new Thread(
            new VerboseRunnable(new Tail(proc))
        );
        thread.setDaemon(true);
        thread.start();
        this.waitFor(socket, config.port());
        if (this.clean) {
            this.configure(config, dist, socket);
        }
        return proc;
    }

    private void prepareFolders(final File target) throws IOException {
        if (this.clean && target.exists()) {
            FileUtils.deleteDirectory(target);
            Logger.info(this, "deleted %s directory", target);
        }
        if (!target.exists() && target.mkdirs()) {
            Logger.info(this, "created %s directory", target);
        }
        final File temp = new File(target, "temp");
        if (!temp.exists() && !temp.mkdirs()) {
            throw new IllegalStateException(
                "Error during temporary folder creation"
            );
        }
    }

    private File data(final File dist, final File target) throws IOException {
        final File dir = new File(target, Instances.DATA_SUB_DIR);
        if (!dir.exists()) {
            final File cnf = new File(
                new File(dist, "share"),
                "my-default.cnf"
            );
            FileUtils.writeStringToFile(
                cnf,
                String.format("[mysql]%n# no defaults..."),
                StandardCharsets.UTF_8
            );
            if (Files.exists(
                Paths.get(dist.getAbsolutePath())
                    .resolve("scripts/mysql_install_db")
            )) {
                new VerboseProcess(
                    this.builder(
                        dist,
                        "scripts/mysql_install_db",
                        String.format("--defaults-file=%s", cnf),
                        "--force",
                        "--innodb_use_native_aio=0",
                        String.format("--datadir=%s", dir),
                        String.format("--basedir=%s", dist)
                    )
                ).stdout();
            } else {
                new VerboseProcess(
                    this.builder(
                        dist,
                        "bin/mysqld",
                        "--initialize-insecure",
                        String.format("--user=%s", Instances.DEFAULT_USER),
                        String.format("--datadir=%s", dir),
                        String.format("--basedir=%s", dist),
                        String.format("--log-error=%s", new File(target, "errors.log")),
                        String.format("--general-log-file=%s", new File(target, "mysql.log"))
                    )
                ).stdout();
            }
        }
        return dir;
    }

    private File waitFor(final File socket, final int port) throws IOException {
        final long start = System.currentTimeMillis();
        long age = 0L;
        while (true) {
            if (socket.exists()) {
                Logger.info(
                    this,
                    "Socket %s is available after %[ms]s of waiting",
                    socket, age
                );
                break;
            }
            if (SocketHelper.isOpen(port)) {
                Logger.info(
                    this,
                    "Port %s is available after %[ms]s of waiting",
                    port, age
                );
                break;
            }
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(ex);
            }
            age = System.currentTimeMillis() - start;
            if (age > TimeUnit.MINUTES.toMillis(5L)) {
                throw new IOException(
                    Logger.format(
                        "Socket %s is not available after %[ms]s of waiting",
                        socket, age
                    )
                );
            }
        }
        return socket;
    }

    private void configure(@NotNull final Config config,
        final File dist, final File socket)
        throws IOException {
        new VerboseProcess(
            this.builder(
                dist,
                "bin/mysqladmin",
                Instances.NO_DEFAULTS,
                String.format("--wait=%d", Instances.RETRY_COUNT),
                String.format("--port=%d", config.port()),
                String.format("--user=%s", Instances.DEFAULT_USER),
                String.format("--socket=%s", socket),
                String.format("--host=%s", Instances.DEFAULT_HOST),
                "password",
                Instances.DEFAULT_PASSWORD
            )
        ).stdout();
        Logger.info(
            this,
            "Root password '%s' set for the '%s' user",
            Instances.DEFAULT_PASSWORD,
            Instances.DEFAULT_USER
        );
        final Process process =
            this.builder(
                dist,
                "bin/mysql",
                String.format("--port=%d", config.port()),
                String.format("--user=%s", Instances.DEFAULT_USER),
                String.format("--password=%s", Instances.DEFAULT_PASSWORD),
                String.format("--socket=%s", socket)
            ).start();
        try (
            PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(
                    process.getOutputStream(),
                    StandardCharsets.UTF_8
                )
            )
        ) {
            writer.print("CREATE DATABASE ");
            writer.print(config.dbname());
            writer.println(";");
            if (!Instances.DEFAULT_USER.equals(config.user())) {
                writer.println(
                    String.format(
                        "CREATE USER '%s'@'%s' IDENTIFIED BY '%s';",
                        config.user(),
                        Instances.DEFAULT_HOST,
                        config.password()
                    )
                );
                writer.println(
                    String.format(
                        "GRANT ALL ON %s.* TO '%s'@'%s';",
                        config.dbname(),
                        config.user(),
                        Instances.DEFAULT_HOST
                    )
                );
                writer.println("SHOW DATABASES;");
            }
        }
        new VerboseProcess(process).stdout();
        Logger.info(
            this,
            "The '%s' user created in the '%s' database with the '%s' password",
            config.user(),
            config.dbname(),
            config.password()
        );
    }

    private ProcessBuilder builder(final File dist, final String name,
        final String... cmds) {
        String label = name;
        final Collection<String> commands = new ArrayList<>(0);
        final File exec = new File(dist, label);
        if (exec.exists()) {
            try {
                exec.setExecutable(true);
            } catch (final SecurityException sex) {
                throw new IllegalStateException(sex);
            }
        } else {
            label = String.format("%s.exe", name);
            if (!new File(dist, label).exists()) {
                label = String.format("%s.pl", name);
                commands.add("perl");
            }
        }
        commands.add(new File(dist, label).getAbsolutePath());
        commands.addAll(Arrays.asList(cmds));
        Logger.info(this, "$ %s", StringUtils.join(commands, " "));
        return new ProcessBuilder()
            .command(commands.toArray(new String[0]))
            .directory(dist);
    }

    private void setClean(final File target, final boolean deldir) {
        if (new File(target, Instances.DATA_SUB_DIR).exists() && !deldir) {
            Logger.info(this, "reuse existing database %s", target);
            this.clean = false;
        } else {
            this.clean = true;
        }
        Logger.info(this, "reuse existing database %s", !this.clean);
    }
}
