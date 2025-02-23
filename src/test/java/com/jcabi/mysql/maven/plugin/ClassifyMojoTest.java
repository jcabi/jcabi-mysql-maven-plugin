/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.mysql.maven.plugin;

import java.util.Properties;
import org.apache.maven.project.MavenProject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link ClassifyMojo} (more detailed test is in maven invoker).
 * @since 0.6
 */
final class ClassifyMojoTest {

    /**
     * ClassifyMojo can detect current platform.
     * @throws Exception If something is wrong
     */
    @Test
    void detectsCurrentPlatform() throws Exception {
        final Properties props = new Properties();
        final MavenProject project = Mockito.mock(MavenProject.class);
        Mockito.doReturn(props).when(project).getProperties();
        final ClassifyMojo mojo = new ClassifyMojo();
        mojo.setProject(project);
        final String name = "test.test";
        mojo.setClassifier(name);
        mojo.execute();
        MatcherAssert.assertThat(
            props.getProperty(name).matches("[a-z]+-[a-z0-9_]+"),
            Matchers.is(true)
        );
    }

}
