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

import de.logrifle.data.parsing.TimeStampFormats;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TestLogWriter {
    public final LogbackMock logger;
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
        logger = new LogbackMock(outfileName != null ? outfileName : "out"+System.getProperty("file.separator")+"log.log");
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
            try {
                writeRandomLogLine();
                scheduleNext(maxDelayMs, maxLineCount - 1, f);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, delayMs, TimeUnit.MILLISECONDS);
    }

    void writeRandomLines(int count) throws IOException {
        for (int i = 0; i < count; i++) {
            writeRandomLogLine();
        }
    }

    void writeRandomLogLine() throws IOException {
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
            logger.error(message);
        } else if (debugLevel < 20) {
            logger.warn(message);
        } else if (debugLevel < 40) {
            logger.info(message);
        } else {
            logger.debug(message);
        }
    }

    void writeException(String message, String exceptionMessage) {
        logger.error(message, new RuntimeException(exceptionMessage));
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
        testLogWriter.start(1, 100);
    }

    private static class LogbackMock {

        private final PrintStream out;
        private final DateTimeFormatter dateTimeFormatter;


        LogbackMock(String logfileName) throws IOException {
            out = new PrintStream(logfileName);
            dateTimeFormatter = DateTimeFormatter.ofPattern(TimeStampFormats.MILLIS_DATE_FORMAT)
                    .withZone(ZoneId.systemDefault());
        }

        private void msg(String level, String message) {
            String timestamp = dateTimeFormatter.format(Instant.now());
            String threadName = Thread.currentThread().getName();
            out.println(timestamp + " [" + threadName + "] " + level + " de.rochefort.logrifle.TestLogWriter - " + message);
            out.flush();
        }

        public void info(String message) {
            msg("INFO ", message);
        }

        public void debug(String message) {
            msg("DEBUG", message);
        }

        public void warn(String message) {
            msg("WARN ", message);
        }

        public void error(String message) {
            msg("ERROR", message);
        }

        public void error(String message, Exception exception) {
            msg("ERROR", message);
            exception.printStackTrace(out);
            out.flush();
        }

        public void close() {
            out.close();
        }
    }
}
