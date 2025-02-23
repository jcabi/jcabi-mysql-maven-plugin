/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.mysql.maven.plugin;

import org.junit.jupiter.api.Test;

/**
 * Test case for {@link StopMojo} (more detailed test is in maven invoker).
 * @since 0.6
 */
final class StopMojoTest {

    /**
     * StopMojo can skip execution when flag is set.
     * @throws Exception If something is wrong
     */
    @Test
    void skipsExecutionWhenRequired() throws Exception {
        final StopMojo mojo = new StopMojo();
        mojo.setSkip(true);
        mojo.execute();
    }

}
