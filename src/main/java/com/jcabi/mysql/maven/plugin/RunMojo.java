/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.mysql.maven.plugin;

import com.jcabi.log.Logger;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Run MySQL in background and don't stop it when Maven is finished.
 * @since 0.4
 */
@ToString
@EqualsAndHashCode(callSuper = false)
@Mojo(
    threadSafe = true, name = "run",
    defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST
)
public final class RunMojo extends AbstractMysqlMojo {

    @Override
    public void run(final Instances instances) throws MojoFailureException {
        final Config config = this.config();
        try {
            instances.start(
                config,
                this.distDir(),
                this.dataDir(),
                this.clear(),
                this.socketFile()
            );
        } catch (final IOException ex) {
            throw new MojoFailureException(
                "failed to start MySQL server", ex
            );
        }
        Logger.info(this, "MySQL is up and running on port %d", config.port());
        Logger.info(
            this,
            "User: %s, password: %s",
            config.user(), config.password()
        );
        Logger.info(this, "Press Ctrl-C to stop...");
        while (true) {
            try {
                TimeUnit.MINUTES.sleep(1L);
            } catch (final InterruptedException ex) {
                throw new MojoFailureException("MySQL terminated", ex);
            }
        }
    }

}
