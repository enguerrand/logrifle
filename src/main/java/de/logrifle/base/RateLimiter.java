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

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RateLimiter {
    private final Runnable task;
    private final Executor executor;
    private final ScheduledExecutorService timerPool;
    private final long minDelayMs;
    private long lastExecutionTime = 0L;
    private boolean jobsPending = false;

    public RateLimiter(Runnable task, Executor singleThreadedExecutor, ScheduledExecutorService timerPool, long minDelayMs) {
        this.task = task;
        this.executor = singleThreadedExecutor;
        this.timerPool = timerPool;
        this.minDelayMs = minDelayMs;
    }

    public void requestExecution() {
        this.executor.execute(() -> {
            long now = System.currentTimeMillis();
            long elapsedSinceLastExecution = now - lastExecutionTime;
            if (elapsedSinceLastExecution < minDelayMs) {
                if (!jobsPending) {
                    jobsPending = true;
                    timerPool.schedule(this::requestExecution, minDelayMs - elapsedSinceLastExecution, TimeUnit.MILLISECONDS);
                }
                return;
            }
            lastExecutionTime = System.currentTimeMillis();
            task.run();
            jobsPending = false;
        });
    }
}
