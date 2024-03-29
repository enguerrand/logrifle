/*
 *  Copyright 2019, Enguerrand de Rochefort
 *
 * This file is part of logrifle.
 *
 * logrifle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * logrifle is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with logrifle.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package de.logrifle.ui;

import com.googlecode.lanterna.gui2.TextGUIThread;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class UI {
    private static volatile boolean TEST_MODE = false;
    private static TextGUIThread THREAD = null;
    private final static CompletableFuture<Void> INITIALIZED_FUTURE = new CompletableFuture<>();

    public static void initialize(TextGUIThread textGUIThread) {
        if (THREAD != null) {
            throw new IllegalStateException("Double initialization");
        }
        THREAD = textGUIThread;
        INITIALIZED_FUTURE.complete(null);
    }

    public static void checkGuiThreadOrThrow() {
        if (TEST_MODE) {
            return;
        }
        if (!isGuiThread()) {
            throw new IllegalStateException("This method must be called on the gui thread!");
        }
    }

    public static boolean isGuiThread() {
        return THREAD != null && Objects.equals(Thread.currentThread(), THREAD.getThread());
    }

    public static CompletableFuture<Void> awaitInitialized() {
        return INITIALIZED_FUTURE;
    }

    public static void runLater(Runnable runnable) {
        if (THREAD == null) {
            throw new IllegalStateException("Not initialized!");
        }
        THREAD.invokeLater(runnable);
    }

    public static void setTestMode() {
        TEST_MODE = true;
    }
}
