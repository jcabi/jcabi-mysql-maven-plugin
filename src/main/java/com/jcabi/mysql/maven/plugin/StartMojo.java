/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.mysql.maven.plugin;

import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Starts MySQL.
 * @since 0.1
 */
@ToString
@EqualsAndHashCode(callSuper = false)
@Mojo(
    threadSafe = true, name = "start",
    defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST
)
public final class StartMojo extends AbstractMysqlMojo {

    @Override
    public void run(final Instances instances) throws MojoFailureException {
        try {
            instances.start(
                this.config(),
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
    }

}
