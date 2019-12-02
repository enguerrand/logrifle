package de.rochefort.logrifle;

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
    public static final Logger LOGGER = LoggerFactory.getLogger(TestLogWriter.class);
    public static final Marker MARKER = MarkerFactory.getMarker("TEST");

    private final ScheduledExecutorService executorService;
    private final Random rTime;
    private final Random rContent;
    private final List<String> dictionary;
    private volatile boolean stopRequested;

    public TestLogWriter(@Nullable Long timingSeed, @Nullable Long contentSeed) throws IOException {
        executorService = Executors.newScheduledThreadPool(1);
        rTime = new Random(timingSeed != null ? timingSeed : System.currentTimeMillis());
        rContent = new Random(contentSeed != null ? contentSeed : System.currentTimeMillis());
        dictionary = readDictionary();
    }

    private static long computeDelayMs(double minimumRateLinesPerSecond) {
        return (long) (1000.0 / minimumRateLinesPerSecond);
    }

    public CompletableFuture<Void> start(double minimumRateLinesPerSecond, @Nullable Integer maxLineCount) {
        CompletableFuture<Void> f = new CompletableFuture<>();
        stopRequested = false;
        long maxDelayMs = computeDelayMs(minimumRateLinesPerSecond);
        scheduleNext(maxDelayMs, maxLineCount == null ? -1 : maxLineCount, f);
        return f;
    }

    public void stop(){
        stopRequested = true;
        executorService.shutdown();
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

    public void writeRandomLines(int count) {
        for (int i = 0; i < count; i++) {
            writeRandomLogLine();
        }
    }

    public void writeRandomLogLine() {
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
        if (debugLevel < 2) {
            writeException(message, "Oops, something went wrong!");
        } else if (debugLevel < 12) {
            LOGGER.error(MARKER, message);
        } else if (debugLevel < 20) {
            LOGGER.warn(MARKER, message);
        } else if (debugLevel < 40) {
            LOGGER.info(MARKER, message);
        } else {
            LOGGER.debug(MARKER, message);
        }
    }

    public void writeException(String message, String exceptionMessage) {
        LOGGER.error(MARKER, message, new RuntimeException(exceptionMessage));
    }

    private String nextRandomWord(){
        int index = rContent.nextInt(dictionary.size());
        return dictionary.get(index);
    }

    private static List<String> readDictionary() throws IOException {
        Path path = Paths.get("./src/test/resources/de/rochefort/logrifle/dictionary.txt");
        return Files.readAllLines(path);
    }

    public static void main(String[] args) throws IOException {
        TestLogWriter testLogWriter = new TestLogWriter(0L, 0L);
        testLogWriter.start(100, null);
    }
}
