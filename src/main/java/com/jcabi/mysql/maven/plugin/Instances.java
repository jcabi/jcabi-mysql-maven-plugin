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

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseProcess;
import com.jcabi.log.VerboseRunnable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;

/**
 * Running instances of MySQL.
 *
 * <p>The class is thread-safe.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 * @checkstyle MultipleStringLiterals (500 lines)
 * @since 0.1
 */
@ToString
@EqualsAndHashCode(of = "processes")
@Loggable(Loggable.INFO)
@SuppressWarnings("PMD.DoNotUseThreads")
final class Instances {

    /**
     * No defaults.
     */
    private static final String NO_DEFAULTS = "--no-defaults";

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
    private final transient ConcurrentMap<Integer, Process> processes =
        new ConcurrentHashMap<Integer, Process>(0);

    /**
     * Start a new one at this port.
     * @param config Instance configuration
     * @param dist Path to MySQL distribution
     * @param target Where to keep temp data
     * @throws IOException If fails to start
     * @checkstyle ParameterNumberCheck (10 lines)
     */
    public void start(@NotNull final Config config, @NotNull final File dist,
        @NotNull final File target) throws IOException {
        synchronized (this.processes) {
            if (this.processes.containsKey(config.port())) {
                throw new IllegalArgumentException(
                    String.format("port %d is already busy", config.port())
                );
            }
            final Process proc = this.process(config, dist, target);
            this.processes.put(config.port(), proc);
            Runtime.getRuntime().addShutdownHook(
                new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            Instances.this.stop(config.port());
                        }
                    }
                )
            );
        }
    }

    /**
     * Stop a running one at this port.
     * @param port The port to stop at
     */
    public void stop(final int port) {
        synchronized (this.processes) {
            final Process proc = this.processes.get(port);
            if (proc != null) {
                proc.destroy();
                this.processes.remove(proc);
            }
        }
    }

    /**
     * Start a new process.
     * @param config Instance configuration
     * @param dist Path to MySQL distribution
     * @param target Where to keep temp data
     * @return Process started
     * @throws IOException If fails to start
     * @checkstyle ParameterNumberCheck (10 lines)
     */
    private Process process(@NotNull final Config config,
        final File dist, final File target)
        throws IOException {
        if (target.exists()) {
            FileUtils.deleteDirectory(target);
            Logger.info(this, "deleted %s directory", target);
        }
        if (target.mkdirs()) {
            Logger.info(this, "created %s directory", target);
        }
        new File(target, "temp").mkdirs();
        final File socket = new File(target, "mysql.sock");
        final ProcessBuilder builder = this.builder(
            dist,
            "bin/mysqld",
            Instances.NO_DEFAULTS,
            "--general_log",
            "--console",
            "--innodb_buffer_pool_size=64M",
            "--innodb_log_file_size=64M",
            "--log_warnings",
            "--innodb_use_native_aio=0",
            String.format("--binlog-ignore-db=%s", config.dbname()),
            String.format("--basedir=%s", dist),
            String.format("--lc-messages-dir=%s", new File(dist, "share")),
            String.format("--datadir=%s", this.data(dist, target)),
            String.format("--tmpdir=%s", new File(target, "temp")),
            String.format("--socket=%s", socket),
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
            new VerboseRunnable(
                new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        new VerboseProcess(proc).stdoutQuietly();
                        return null;
                    }
                }
            )
        );
        thread.setDaemon(true);
        thread.start();
        this.configure(config, dist, this.waitFor(socket, config.port()));
        return proc;
    }

    /**
     * Prepare and return data directory.
     * @param dist Path to MySQL distribution
     * @param target Where to create it
     * @return Directory created
     * @throws IOException If fails
     */
    private File data(final File dist, final File target) throws IOException {
        final File dir = new File(target, "data");
        FileUtils.writeStringToFile(
            new File(dist, "support-files/my-default.cnf"),
            "[mysql]\n# no defaults..."
        );
        new VerboseProcess(
            this.builder(
                dist,
                "scripts/mysql_install_db",
                Instances.NO_DEFAULTS,
                "--force",
                "--innodb_use_native_aio=0",
                String.format("--datadir=%s", dir)
            )
        ).stdoutQuietly();
        return dir;
    }

    /**
     * Wait for this file to become available.
     * @param socket The file to wait for
     * @param port Port to wait for
     * @return The same socket, but ready for usage
     * @throws IOException If fails
     */
    private File waitFor(final File socket, final int port) throws IOException {
        final long start = System.currentTimeMillis();
        long age = 0L;
        while (true) {
            if (socket.exists()) {
                Logger.info(
                    this,
                    "socket %s is available after %[ms]s of waiting",
                    socket, age
                );
                break;
            }
            if (Instances.isOpen(port)) {
                Logger.info(
                    this,
                    "port %s is available after %[ms]s of waiting",
                    port, age
                );
                break;
            }
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(ex);
            }
            age = System.currentTimeMillis() - start;
            if (age > TimeUnit.MINUTES.toMillis(1L)) {
                throw new IOException(
                    Logger.format(
                        "socket %s is not available after %[ms]s of waiting",
                        socket, age
                    )
                );
            }
        }
        return socket;
    }

    /**
     * Configure the running MySQL server.
     * @param config Instance configuration
     * @param dist Directory with MySQL distribution
     * @param socket Socket of it
     * @throws IOException If fails
     */
    private void configure(@NotNull final Config config,
        final File dist, final File socket)
        throws IOException {
        new VerboseProcess(
            this.builder(
                dist,
                "bin/mysqladmin",
                Instances.NO_DEFAULTS,
                String.format("--port=%d", config.port()),
                String.format("--user=%s", Instances.DEFAULT_USER),
                String.format("--socket=%s", socket),
                String.format("--host=%s", Instances.DEFAULT_HOST),
                "password",
                Instances.DEFAULT_PASSWORD
            )
        ).stdoutQuietly();
        final Process process =
            this.builder(
                dist,
                "bin/mysql",
                String.format("--port=%d", config.port()),
                String.format("--user=%s", Instances.DEFAULT_USER),
                String.format("--password=%s", Instances.DEFAULT_PASSWORD),
                String.format("--socket=%s", socket)
            ).start();
        final PrintWriter writer = new PrintWriter(
            new OutputStreamWriter(
                process.getOutputStream(), CharEncoding.UTF_8
            )
        );
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
        }
        writer.close();
        new VerboseProcess(process).stdoutQuietly();
    }

    /**
     * Make process builder with this commands.
     * @param dist Distribution directory
     * @param name Name of the cmd to run
     * @param cmds Commands
     * @return Process builder
     */
    private ProcessBuilder builder(final File dist, final String name,
        final String... cmds) {
        String label = name;
        final Collection<String> commands = new LinkedList<String>();
        if (!new File(dist, label).exists()) {
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
            .command(commands.toArray(new String[commands.size()]))
            .directory(dist);
    }

    /**
     * Port is open.
     * @param port The port to check
     * @return TRUE if it's open
     */
    private static boolean isOpen(final int port) {
        boolean open;
        try {
            new Socket((String) null, port);
            open = true;
        } catch (IOException ex) {
            open = false;
        }
        return open;
    }

}
