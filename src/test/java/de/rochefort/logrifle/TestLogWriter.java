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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TestLogWriter {
    public static final Logger LOGGER = LoggerFactory.getLogger(TestLogWriter.class);
    public static final Marker MARKER = MarkerFactory.getMarker("TEST");

    private final long maxLineDelayMs;
    private final ScheduledExecutorService executorService;
    private final Random r;
    private final List<String> dictionary;

    public TestLogWriter(double minimumRateLinesPerSecond, @Nullable Long seed) throws IOException {
        this.maxLineDelayMs = (long) (1000.0 / minimumRateLinesPerSecond);
        executorService = Executors.newScheduledThreadPool(1);
        r = new Random(seed != null ? seed : System.currentTimeMillis());
        dictionary = readDictionary();
    }

    public void start() {
        scheduleNext();
    }

    private void scheduleNext() {
        long delayMs = r.nextLong() % this.maxLineDelayMs;
        executorService.schedule(() -> {
            writeRandomLogLine();
            scheduleNext();
        }, delayMs, TimeUnit.MILLISECONDS);
    }

    private void writeRandomLogLine() {
        int wordCount = r.nextInt(20);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < wordCount; i++) {
            sb.append(nextRandomWord());
            if (i != wordCount-1) {
                sb.append(" ");
            }
        }
        int debugLevel = r.nextInt(100);
        String message = sb.toString();
        if (debugLevel < 2) {
            LOGGER.error(MARKER, message);
        } else if (debugLevel < 10) {
            LOGGER.warn(MARKER, message);
        } else if (debugLevel < 40) {
            LOGGER.info(MARKER, message);
        } else {
            LOGGER.debug(MARKER, message);
        }
    }

    private String nextRandomWord(){
        int index = r.nextInt(dictionary.size());
        return dictionary.get(index);
    }

    private static List<String> readDictionary() throws IOException {
        Path path = Paths.get("./src/test/resources/de/rochefort/logrifle/dictionary.txt");
        return Files.readAllLines(path);
    }

    public static void main(String[] args) throws IOException {
        TestLogWriter testLogWriter = new TestLogWriter(1, 0L);
        testLogWriter.start();
    }
}
