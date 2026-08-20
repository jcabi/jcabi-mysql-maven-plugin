/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.mysql.maven.plugin;

import com.jcabi.log.VerboseProcess;
import java.util.concurrent.Callable;

/**
 * Consumer of the standard output of one running MySQL.
 * @since 0.1
 */
final class Tail implements Callable<Void> {

    /**
     * Process to read from.
     */
    private final transient Process proc;

    /**
     * Ctor.
     * @param process The process to read from
     */
    Tail(final Process process) {
        this.proc = process;
    }

    @Override
    public Void call() {
        new VerboseProcess(this.proc).stdoutQuietly();
        return null;
    }
}
