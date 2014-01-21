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
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Run MySQL in background and don't stop it when Maven is finished.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
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
    protected void run(final Instances instances) throws MojoFailureException {
        final Config config = this.config();
        try {
            instances.start(
                config,
                this.distDir(),
                this.dataDir()
            );
        } catch (IOException ex) {
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
            } catch (InterruptedException ex) {
                throw new MojoFailureException("MySQL terminated", ex);
            }
        }
    }

}
