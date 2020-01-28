/*
 *  Copyright 2020, Enguerrand de Rochefort
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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimiterFactoryTestImpl implements RateLimiterFactory {
    private final CountDownLatch jobsCountdown;
    private final AtomicInteger jobsCounter = new AtomicInteger(0);
    private final List<Executor> executors = new CopyOnWriteArrayList<>();

    public RateLimiterFactoryTestImpl(int expectedJobCount) {
        this.jobsCountdown = new CountDownLatch(expectedJobCount);
    }


    @Override
    public RateLimiter newRateLimiter(Runnable task, Executor singleThreadedExecutor) {
        if (!executors.contains(singleThreadedExecutor)) {
            executors.add(singleThreadedExecutor);
        }
        return () -> {
            singleThreadedExecutor.execute(task);
            if (!(task instanceof UncountedRunnable)) {
                jobsCounter.incrementAndGet();
                jobsCountdown.countDown();
            }
        };
    }

    public void awaitJobsDone() throws InterruptedException {
        jobsCountdown.await();
        List<Executor> executors = this.executors;
        CountDownLatch latch = new CountDownLatch(executors.size());
        for (Executor executor : executors) {
                executor.execute(new UncountedRunnable() {
                    @Override
                    public void run() {
                        latch.countDown();
                    }
                });
        }
        latch.await();
    }

    public int getExecutedJobCount() {
        return jobsCounter.get();
    }

    private interface UncountedRunnable extends Runnable {
    }
}
