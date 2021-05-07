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

package de.logrifle.base;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LogDispatcher implements Executor {
    private final Executor singleThreadedExecutor;
    public static final String THREAD_NAME = "LDP";

    public LogDispatcher() {
        singleThreadedExecutor = Executors.newSingleThreadExecutor(r -> new Thread(r, THREAD_NAME));
    }

    public void checkOnDispatchThreadOrThrow() {
        if (!isOnThread()) {
            throw new IllegalStateException("Not on dispatch thread! Current Thread: " + Thread.currentThread().getName() + " - expected: " + THREAD_NAME);
        }
    }

    @Override
    public void execute(@NotNull Runnable runnable) {
        if (isOnThread()) {
            runnable.run();
        } else {
            singleThreadedExecutor.execute(runnable);
        }
    }

    public boolean isOnThread() {
        return Objects.equals(THREAD_NAME, Thread.currentThread().getName());
    }
}
