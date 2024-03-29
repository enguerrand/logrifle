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

import com.googlecode.lanterna.TextColor;
import de.logrifle.base.DirectDispatcher;
import de.logrifle.base.LogDispatcher;
import de.logrifle.base.RateLimiterFactoryTestImpl;
import de.logrifle.data.parsing.Line;
import de.logrifle.data.parsing.LineParser;
import de.logrifle.data.parsing.LineParserTextImpl;
import de.logrifle.data.parsing.LineParserTimestampedTextImpl;
import de.logrifle.data.parsing.TimeStampFormat;
import de.logrifle.data.parsing.TimeStampFormats;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("ConstantConditions")
class LogReaderTest {
    static Path LOGFILE = Paths.get("./out/log.log");
    static ExecutorService WORKER_POOL;
    static final Charset charset = StandardCharsets.UTF_8;

    @TempDir
    Path tempDir;

    @BeforeAll
    static void setUp() {
        WORKER_POOL = Executors.newCachedThreadPool();
    }

    @BeforeEach
    void setUpEach() throws IOException {
        if (Files.exists(LOGFILE)){
            Files.write(LOGFILE, new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    @AfterAll
    static void tearDown() {
        WORKER_POOL.shutdown();
    }

    @Test
    void testReadStaticLog() throws Exception {
        LogDispatcher logDispatcher = new LogDispatcher();
        int lineCount = 3;
        RateLimiterFactoryTestImpl rateLimiterFactory = new RateLimiterFactoryTestImpl(lineCount);
        TestLogWriter logWriter = new TestLogWriter(null, 0L, null);
        for (int i = 0; i < lineCount; i++) {
            logWriter.writeRandomLogLine();
        }
        logWriter.stop();
        LogReader logReader = new LogReader(new LineParserTextImpl(), LOGFILE, TextColor.ANSI.DEFAULT, WORKER_POOL, logDispatcher, rateLimiterFactory, charset);
        rateLimiterFactory.awaitJobsDone();
        Assertions.assertEquals(lineCount, rateLimiterFactory.getExecutedJobCount());
        Assertions.assertEquals(3, logReader.getLineCount(), "wrong line count");
        Assertions.assertTrue(logReader.getLine(1).getRaw().endsWith("ullam doloremque quia dolorem pariatur. adipiscing 0076.32 nesciunt. dolore"), "Wrong line content");
    }

    @Test
    void testOpenDynamicLog() throws Exception {
        LogDispatcher logDispatcher = new LogDispatcher();
        int initialLinesCount = 100;
        int tailedLinesCount = 200;
        int expectedJobCount = initialLinesCount + tailedLinesCount;
        RateLimiterFactoryTestImpl rateLimiterFactory = new RateLimiterFactoryTestImpl(expectedJobCount);
        TestLogWriter logWriter = new TestLogWriter(null, 0L, null, false);
        logWriter.writeRandomLines(initialLinesCount);
        CompletableFuture<Void> f = logWriter.start(100, tailedLinesCount);
        LogReader logReader = new LogReader(new LineParserTimestampedTextImpl(), LOGFILE, TextColor.ANSI.DEFAULT, WORKER_POOL, logDispatcher, rateLimiterFactory, charset);
        f.get();
        rateLimiterFactory.awaitJobsDone();
        Assertions.assertEquals(expectedJobCount, rateLimiterFactory.getExecutedJobCount());
        Assertions.assertTrue(logReader.getLine(1).getRaw().endsWith("ullam doloremque quia dolorem pariatur. adipiscing 0076.32 nesciunt. dolore"), "Wrong line content");
        Assertions.assertTrue(logReader.getLine(99).getRaw().endsWith("laboriosam, autem minima ut est ad qui veritatis sunt dolore in sit quae labore öäßaweawe( consequat."), "Wrong line content");
        Assertions.assertTrue(logReader.getLine(100).getRaw().endsWith("aperiam, pariatur. veniam,"), "Wrong line content");
        Assertions.assertTrue(logReader.getLine(299).getRaw().endsWith("eum qui nostrud ut"), "Wrong line content");
        logWriter.stop();
    }

    @Test
    void testReadException() throws Exception {
        LogDispatcher logDispatcher = new LogDispatcher();
        RateLimiterFactoryTestImpl rateLimiterFactory = new RateLimiterFactoryTestImpl(2);
        TestLogWriter logWriter = new TestLogWriter(null, 0L, null);
        logWriter.writeException("Exception text", "Exception Message");
        logWriter.stop();
        LogReader logReader = new LogReader(new LineParserTimestampedTextImpl(), LOGFILE, TextColor.ANSI.DEFAULT, WORKER_POOL, logDispatcher, rateLimiterFactory, charset);
        List<Line> lines = logReader.getLines();
        rateLimiterFactory.awaitJobsDone();
        Assertions.assertEquals(1, lines.size(), "no lines were appended");
        Assertions.assertNotEquals(0, lines.get(0).getAdditionalLines().size(), "additional lines missing");
        Assertions.assertEquals("java.lang.RuntimeException: Exception Message", lines.get(0).getAdditionalLines().get(0), "wrong first additional line");
    }

    @Test
    void testGetLines() throws Exception {
        LogDispatcher logDispatcher = new LogDispatcher();
        int lineCount = 5;
        RateLimiterFactoryTestImpl rateLimiterFactory = new RateLimiterFactoryTestImpl(lineCount);
        TestLogWriter logWriter = new TestLogWriter(null, 0L, null);
        for (int i = 0; i < lineCount; i++) {
            logWriter.writeRandomLogLine();
        }
        logWriter.stop();
        LogReader logReader = new LogReader(new LineParserTextImpl(), LOGFILE, TextColor.ANSI.DEFAULT, WORKER_POOL, logDispatcher, rateLimiterFactory, charset);

        rateLimiterFactory.awaitJobsDone();
        Assertions.assertEquals(lineCount, rateLimiterFactory.getExecutedJobCount());
        Assertions.assertEquals(0, logReader.getLines(5, 3).size(), "completely out of bounds index");
        Assertions.assertEquals(1, logReader.getLines(4, 2).size(), "partially out of bounds index");
        Assertions.assertEquals(2, logReader.getLines(3, 2).size(), "at bounds index");
        Assertions.assertEquals(3, logReader.getLines(2, 3).size(), "at bounds index");
        Assertions.assertEquals(2, logReader.getLines(2, 2).size(), "in bounds index");
        Assertions.assertEquals(lineCount, logReader.getLines(0, 8).size(), "too large range result size");
    }

    @Test
    void  logReaderShouldMaintainIncomingOrder() throws IOException, InterruptedException {
        LogDispatcher dispatcher = new DirectDispatcher();
        LineParser parser = new LineParserTimestampedTextImpl(
                new TimeStampFormat(TimeStampFormats.MILLIS_TIME_MATCH_REGEX, TimeStampFormats.MILLIS_DATE_FORMAT)
        );
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Path logfile = tempDir.resolve("log1.log");
        RateLimiterFactoryTestImpl factory = new RateLimiterFactoryTestImpl(4);
        LogReader logReader = new LogReader(parser, logfile, TextColor.ANSI.RED, executorService, dispatcher, factory, StandardCharsets.UTF_8);
        writeLogLine(logfile, "23:20:58.268 [main] DEBUG de.logrifle.data.io.TestLogWriter - line 0");
        writeLogLine(logfile, "23:50:58.268 [main] DEBUG de.logrifle.data.io.TestLogWriter - line 1");
        writeLogLine(logfile, "00:07:18.268 [main] DEBUG de.logrifle.data.io.TestLogWriter - line 2");
        writeLogLine(logfile, "01:07:18.268 [main] DEBUG de.logrifle.data.io.TestLogWriter - line 3");
        factory.awaitJobsDone();
        List<Line> read = logReader.getAllLines();
        for (int i = 0, readSize = read.size(); i < readSize; i++) {
            Line line = read.get(i);
            Assertions.assertTrue(line.getRaw().endsWith(String.valueOf(i)));
        }
    }

    private void writeLogLine(Path logfile, String line) throws IOException {
        Files.write(
                logfile,
                Collections.singleton(line),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }
}