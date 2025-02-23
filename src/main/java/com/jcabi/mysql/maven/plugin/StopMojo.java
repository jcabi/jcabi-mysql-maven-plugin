/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.mysql.maven.plugin;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Stops MySQL.
 *
 * @since 0.1
 */
@ToString
@EqualsAndHashCode(callSuper = false)
@Mojo(
    threadSafe = true, name = "stop",
    defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST
)
public final class StopMojo extends AbstractMysqlMojo {

    @Override
    public void run(final Instances instances) throws MojoFailureException {
        instances.stop(this.config().port());
    }

}
