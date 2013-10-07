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
import com.jcabi.log.VerboseProcess;
import com.jcabi.log.VerboseRunnable;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Running instances of MySQL.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1
 */
@ToString
@EqualsAndHashCode(of = "processes")
@Loggable(Loggable.INFO)
@SuppressWarnings("PMD.DoNotUseThreads")
final class Instances {

    /**
     * Running processes.
     */
    private final transient ConcurrentMap<Integer, Process> processes =
        new ConcurrentHashMap<Integer, Process>(0);

    /**
     * Start a new one at this port.
     * @param port The port to start at
     * @param dist Path to MySQL distribution
     * @throws IOException If fails to start
     */
    public void start(final int port, @NotNull final File dist)
        throws IOException {
        final Process proc = new ProcessBuilder().command(
            new String[] {
                "bin/mysql",
                "--port",
                Integer.toString(port),
            }
        ).directory(dist).redirectErrorStream(true).start();
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

}
