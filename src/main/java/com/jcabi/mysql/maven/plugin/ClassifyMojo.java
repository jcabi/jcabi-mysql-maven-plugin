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

import com.jcabi.log.Logger;
import java.util.Locale;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoParameter;
import org.jfrog.maven.annomojo.annotations.MojoPhase;

/**
 * Classify current platform.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1
 */
@ToString
@EqualsAndHashCode(callSuper = false)
@MojoGoal("classify")
@MojoPhase("initialize")
public final class ClassifyMojo extends AbstractMojo {

    /**
     * Maven project.
     */
    @MojoParameter(
        expression = "${project}",
        required = true,
        readonly = true,
        description = "Maven project"
    )
    private transient MavenProject project;

    /**
     * Classifier to set.
     */
    @MojoParameter(
        defaultValue = "mysql.classifier",
        required = true,
        readonly = false,
        description = "Maven property to set with platform classifier"
    )
    private transient String classifier;

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws MojoFailureException {
        if (this.project.getProperties().getProperty(this.classifier) != null) {
            throw new MojoFailureException(
                String.format(
                    "Maven property ${%s} already set to \"%s\"",
                    this.classifier,
                    this.project.getProperties().getProperty(this.classifier)
                )
            );
        }
        final String[] words = System.getProperty("os.name").split(" ");
        final String value = String.format(
            "%s-%s",
            words[0].toLowerCase(Locale.ENGLISH),
            System.getProperty("os.arch").toLowerCase(Locale.ENGLISH)
        );
        this.project.getProperties().setProperty(this.classifier, value);
        Logger.info(this, "${%s} set to \"%s\"", this.classifier, value);
    }

    /**
     * Set project.
     * @param prj Project to set
     */
    public void setProject(final MavenProject prj) {
        this.project = prj;
    }

    /**
     * Set classifier.
     * @param name Name of property
     */
    public void setClassifier(final String name) {
        this.classifier = name;
    }

}
