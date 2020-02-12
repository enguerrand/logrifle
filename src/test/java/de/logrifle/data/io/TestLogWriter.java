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

package de.logrifle.data.io;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TestLogWriter {
    public final Logger logger;
    public final Marker marker;

    private final ScheduledExecutorService executorService;
    private final Random rTime;
    private final Random rContent;
    private final List<String> dictionary;
    private volatile boolean stopRequested;
    private final boolean exceptionsAllowed;

    public TestLogWriter(@Nullable Long timingSeed, @Nullable Long contentSeed, @Nullable String outfileName) throws IOException {
        this(timingSeed, contentSeed, outfileName, true);
    }

    public TestLogWriter(@Nullable Long timingSeed, @Nullable Long contentSeed, @Nullable String outfileName, boolean exceptionsAllowed) throws IOException {
        System.setProperty("logfile.name", outfileName != null ? outfileName : "log.log");
        logger = LoggerFactory.getLogger(TestLogWriter.class);
        marker = MarkerFactory.getMarker("TEST");
        executorService = Executors.newScheduledThreadPool(1);
        rTime = new Random(timingSeed != null ? timingSeed : System.currentTimeMillis());
        rContent = new Random(contentSeed != null ? contentSeed : System.currentTimeMillis());
        dictionary = readDictionary();
        this.exceptionsAllowed = exceptionsAllowed;
    }

    private static long computeDelayMs(double minimumRateLinesPerSecond) {
        return (long) (1000.0 / minimumRateLinesPerSecond);
    }

    CompletableFuture<Void> start(double minimumRateLinesPerSecond, @Nullable Integer maxLineCount) {
        CompletableFuture<Void> f = new CompletableFuture<>();
        stopRequested = false;
        long maxDelayMs = computeDelayMs(minimumRateLinesPerSecond);
        scheduleNext(maxDelayMs, maxLineCount == null ? -1 : maxLineCount, f);
        return f;
    }

    void stop(){
        stopRequested = true;
        executorService.shutdown();
        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private void scheduleNext(long maxDelayMs, int maxLineCount, CompletableFuture<Void> f) {
        if (stopRequested || (maxLineCount == 0)) {
            f.complete(null);
            return;
        }
        long delayMs = rTime.nextLong() % maxDelayMs;
        executorService.schedule(() -> {
            writeRandomLogLine();
            scheduleNext(maxDelayMs, maxLineCount - 1, f);
        }, delayMs, TimeUnit.MILLISECONDS);
    }

    void writeRandomLines(int count) {
        for (int i = 0; i < count; i++) {
            writeRandomLogLine();
        }
    }

    void writeRandomLogLine() {
        int wordCount = rContent.nextInt(20);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < wordCount; i++) {
            sb.append(nextRandomWord());
            if (i != wordCount-1) {
                sb.append(" ");
            }
        }
        int debugLevel = rContent.nextInt(100);
        String message = sb.toString();
        if (debugLevel < 2 && exceptionsAllowed) {
            writeException(message, "Oops, something went wrong!");
        } else if (debugLevel < 12) {
            logger.error(marker, message);
        } else if (debugLevel < 20) {
            logger.warn(marker, message);
        } else if (debugLevel < 40) {
            logger.info(marker, message);
        } else {
            logger.debug(marker, message);
        }
    }

    void writeException(String message, String exceptionMessage) {
        logger.error(marker, message, new RuntimeException(exceptionMessage));
    }

    private String nextRandomWord(){
        int index = rContent.nextInt(dictionary.size());
        return dictionary.get(index);
    }

    private static List<String> readDictionary() throws IOException {
        Path path = Paths.get("./src/test/resources/de/logrifle/dictionary.txt");
        return Files.readAllLines(path);
    }

    public static void main(String[] args) throws IOException {
        String logfileName = null;
        if (args.length > 0) {
            logfileName = args[0];
        }
        TestLogWriter testLogWriter = new TestLogWriter(null, null, logfileName);
        testLogWriter.start(1, null);
    }
}
