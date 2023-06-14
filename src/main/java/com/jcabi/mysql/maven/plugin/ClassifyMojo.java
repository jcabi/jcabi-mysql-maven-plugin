/*
 * Copyright (c) 2012-2023, jcabi.com
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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Classify current platform.
 *
 * @since 0.1
 */
@ToString
@EqualsAndHashCode(callSuper = false)
@Mojo(
    threadSafe = true, name = "classify",
    defaultPhase = LifecyclePhase.INITIALIZE
)
public final class ClassifyMojo extends AbstractMojo {

    /**
     * Maven project.
     */
    @Parameter(
        defaultValue = "${project}",
        required = true,
        readonly = true
    )
    private transient MavenProject project;

    /**
     * Classifier to set.
     */
    @Parameter(defaultValue = "mysql.classifier", required = true)
    private transient String classifier;

    /**
     * Classification mappings.
     *
     * @since 0.9.0
     * @checkstyle MemberNameCheck (5 lines)
     */
    @Parameter(required = true)
    @SuppressWarnings("PMD.ImmutableField")
    private transient List<String> mappings;

    @Override
    public void execute() throws MojoFailureException {
        if (this.mappings == null) {
            this.mappings = new LinkedList<>();
            this.mappings.add("amd64->aarch64");
            this.mappings.add("i386->x86_64");
        }
        final String[] words = System.getProperty("os.name").split(" ");
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ENGLISH);
        for (final String mapping : this.mappings) {
            final String[] pair = mapping.split("->");
            if (pair.length != 2) {
                throw new MojoFailureException(
                    String.format(
                        "Invalid mapping \"%s\" (should be \"from->to\")",
                        mapping
                    )
                );
            }
            if (arch.equals(pair[0])) {
                arch = pair[1];
                break;
            }
        }
        final String value = String.format(
            "%s-%s", words[0].toLowerCase(Locale.ENGLISH), arch
        );
        final String existing = this.project.getProperties()
            .getProperty(this.classifier);
        if (existing == null) {
            this.project.getProperties().setProperty(this.classifier, value);
            Logger.info(this, "${%s} set to \"%s\"", this.classifier, value);
        } else if (existing.equals(value)) {
            Logger.info(
                this, "${%s} already set to \"%s\"",
                this.classifier, value
            );
        } else {
            throw new MojoFailureException(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "Maven property ${%s} already set to \"%s\", can't change to \"%s\"",
                    this.classifier, existing, value
                )
            );
        }
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
