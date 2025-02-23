/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.mysql.maven.plugin;

import java.io.IOException;
import java.net.Socket;

/**
 * Extracted static helper function into its own class to reduce class
 * complexity of Instances (TooManyMethods).
 *
 * <p>The class is thread-safe.
 * @since 0.6
 */
final class SocketHelper {

    /**
     * Utility class should not be instantiated.
     */
    private SocketHelper() { }

    /**
     * Port is open.
     * @param port The port to check
     * @return TRUE if it's open
     */
    static boolean isOpen(final int port) {
        boolean open;
        try {
            new Socket((String) null, port);
            open = true;
        } catch (final IOException ex) {
            open = false;
        }
        return open;
    }

}
