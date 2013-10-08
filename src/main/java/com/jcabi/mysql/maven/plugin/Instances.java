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
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.util.FileUtils;

/**
 * Running instances of MySQL.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 * @checkstyle MultipleStringLiterals (500 lines)
 */
@ToString
@EqualsAndHashCode(of = "processes")
@Loggable(Loggable.INFO)
@SuppressWarnings("PMD.DoNotUseThreads")
final class Instances {

    /**
     * User.
     */
    private static final String USER = "root";

    /**
     * Password.
     */
    private static final String PASSWORD = "root";

    /**
     * Database name.
     */
    private static final String DBNAME = "root";

    /**
     * Running processes.
     */
    private final transient ConcurrentMap<Integer, Process> processes =
        new ConcurrentHashMap<Integer, Process>(0);

    /**
     * Start a new one at this port.
     * @param port The port to start at
     * @param dist Path to MySQL distribution
     * @param target Where to keep temp data
     * @throws IOException If fails to start
     */
    public void start(final int port, @NotNull final File dist,
        @NotNull final File target) throws IOException {
        new File(target, "temp").mkdirs();
        final File socket = new File(target, "mysql.sock");
        final ProcessBuilder builder = this.builder(
            "bin/mysqld",
            "--basedir=.",
            "--lc-messages-dir=./share",
            "--general_log",
            "--console",
            "--innodb_use_native_aio=0",
            "--innodb_buffer_pool_size=64M",
            "--innodb_log_file_size=64M",
            "--explicit_defaults_for_timestamp",
            "--log_warnings",
            "--binlog-ignore-db=data",
            String.format("--datadir=%s", this.data(dist, target)),
            String.format("--tmpdir=%s/temp", target),
            String.format("--socket=%s", socket),
            String.format("--pid-file=%s/mysql.pid", target),
            String.format("--port=%d", port)
        ).directory(dist).redirectErrorStream(true);
        builder.environment().put("MYSQL_HOME", dist.getAbsolutePath());
        final Process proc = builder.start();
        final Thread thread = new Thread(
            new VerboseRunnable(
                new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        new VerboseProcess(proc).stdout();
                        return null;
                    }
                }
            )
        );
        thread.setDaemon(true);
        thread.start();
        this.processes.put(port, proc);
        this.configure(dist, port, this.waitFor(socket));
    }

    /**
     * Stop a running one at this port.
     * @param port The port to stop at
     */
    public void stop(final int port) {
        final Process proc = this.processes.get(port);
        if (proc == null) {
            throw new IllegalArgumentException(
                String.format(
                    "No MySQL instances running on port %d", port
                )
            );
        }
        proc.destroy();
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
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
            Logger.info(this, "deleted %s directory", dir);
        }
        if (dir.mkdirs()) {
            Logger.info(this, "created %s directory", dir);
        }
        new VerboseProcess(
            this.builder(
                "scripts/mysql_install_db",
                "--no-defaults",
                "--basedir=.",
                "--explicit_defaults_for_timestamp",
                String.format("--datadir=%s", dir)
            ).directory(dist)
        ).stdout();
        return dir;
    }

    /**
     * Wait for this file to become available.
     * @param socket The file to wait for
     * @return The same socket, but ready for usage
     * @throws IOException If fails
     */
    private File waitFor(final File socket) throws IOException {
        final long start = System.currentTimeMillis();
        long age = 0;
        while (!socket.exists()) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(ex);
            }
            age = System.currentTimeMillis() - start;
            if (age > TimeUnit.MINUTES.toMillis(1)) {
                throw new IOException(
                    Logger.format(
                        "socket %s is not available after %[ms]s of waiting",
                        socket, age
                    )
                );
            }
        }
        Logger.format(
            "socket %s is available after %[ms]s of waiting, MySQL is running",
            socket, age
        );
        return socket;
    }

    /**
     * Configure the running MySQL server.
     * @param dist Directory with MySQL distribution
     * @param port The port it's running on
     * @param socket Socket of it
     * @throws IOException If fails
     */
    private void configure(final File dist, final int port, final File socket)
        throws IOException {
        new VerboseProcess(
            this.builder(
                "bin/mysqladmin",
                String.format("--port=%d", port),
                String.format("--user=%s", Instances.USER),
                String.format("--socket=%s", socket),
                "password",
                Instances.PASSWORD
            ).directory(dist)
        ).stdout();
        final Process process = this.builder(
            "bin/mysql",
            String.format("--port=%d", port),
            String.format("--user=%s", Instances.USER),
            String.format("--password=%s", Instances.PASSWORD),
            String.format("--socket=%s", socket)
        ).directory(dist).start();
        final PrintWriter writer = new PrintWriter(
            new OutputStreamWriter(
                process.getOutputStream(), CharEncoding.UTF_8
            )
        );
        writer.print("CREATE DATABASE ");
        writer.print(Instances.DBNAME);
        writer.println(";");
        writer.close();
        new VerboseProcess(process).stdout();
    }

    /**
     * Make process builder with this commands.
     * @param cmds Commands
     * @return Process builder
     */
    private ProcessBuilder builder(final String... cmds) {
        Logger.info(this, "$ %s", StringUtils.join(cmds, " "));
        return new ProcessBuilder().command(cmds);
    }

}
