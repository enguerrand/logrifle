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

package de.logrifle;

import com.googlecode.lanterna.TextColor;
import de.logrifle.data.parsing.LineParserTimestampedTextImpl;
import de.logrifle.base.LogDispatcher;
import de.logrifle.data.parsing.Line;
import de.logrifle.data.parsing.LineParserTextImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BooleanSupplier;

class LogReaderTest {
    static Path LOGFILE = Paths.get("./out/log.log");
    static ExecutorService WORKER_POOL;
    static ScheduledExecutorService TIMER_POOL;
    static LogDispatcher LOG_DISPATCH_EXECUTOR;

    @BeforeAll
    static void setUp() {
        WORKER_POOL = Executors.newCachedThreadPool();
        TIMER_POOL = Executors.newScheduledThreadPool(10);
        LOG_DISPATCH_EXECUTOR = new LogDispatcher();
    }

    @BeforeEach
    void setUpEach() throws IOException {
        if(Files.exists(LOGFILE)){
            Files.write(LOGFILE, new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    @AfterAll
    static void tearDown() throws IOException {
        Files.delete(LOGFILE);
        WORKER_POOL.shutdown();
    }

    @Test
    void testReadStaticLog() throws Exception {
        TestLogWriter logWriter = new TestLogWriter(null, 0L, null);
        logWriter.writeRandomLogLine();
        logWriter.writeRandomLogLine();
        logWriter.writeRandomLogLine();
        logWriter.stop();
        LogReader logReader = new LogReader(new LineParserTextImpl(), LOGFILE, TextColor.ANSI.DEFAULT, WORKER_POOL, TIMER_POOL, LOG_DISPATCH_EXECUTOR);
        await(() -> logReader.getLineCount() == 3, 2_000L);
        Assertions.assertEquals(3, logReader.getLineCount(), "wrong line count");
        Assertions.assertTrue(logReader.getLine(1).getRaw().endsWith("ullam doloremque quia dolorem pariatur. adipiscing 0076.32 nesciunt. dolore"), "Wrong line content");
    }

    @Test
    void testOpenDynamicLog() throws Exception {
        TestLogWriter logWriter = new TestLogWriter(null, 0L, null);
        logWriter.writeRandomLines(100);
        CompletableFuture<Void> f = logWriter.start(100, 200);
        LogReader logReader = new LogReader(new LineParserTimestampedTextImpl(), LOGFILE, TextColor.ANSI.DEFAULT, WORKER_POOL, TIMER_POOL, LOG_DISPATCH_EXECUTOR);
        f.get();
        await(() -> logReader.getLineCount() == 300, 2_000L);
        Assertions.assertEquals(300, logReader.getLineCount(), "wrong line count");
        Assertions.assertTrue(logReader.getLine(1).getRaw().endsWith("ullam doloremque quia dolorem pariatur. adipiscing 0076.32 nesciunt. dolore"), "Wrong line content");
        Assertions.assertTrue(logReader.getLine(99).getRaw().endsWith("laboriosam, autem minima ut est ad qui veritatis sunt dolore in sit quae labore öäßaweawe( consequat."), "Wrong line content");
        Assertions.assertTrue(logReader.getLine(100).getRaw().endsWith("aperiam, pariatur. veniam,"), "Wrong line content");
        Assertions.assertTrue(logReader.getLine(299).getRaw().endsWith("eum qui nostrud ut"), "Wrong line content");
    }

    @Test
    void testReadException() throws Exception {
        TestLogWriter logWriter = new TestLogWriter(null, 0L, null);
        logWriter.writeException("Exception text", "Exception Message");
        logWriter.stop();
        LogReader logReader = new LogReader(new LineParserTimestampedTextImpl(), LOGFILE, TextColor.ANSI.DEFAULT, WORKER_POOL, TIMER_POOL, LOG_DISPATCH_EXECUTOR);
        List<Line> lines = logReader.getLines();
        await(() -> logReader.getAllLines().size() == 1, 2_000L);
        Assertions.assertEquals(1, lines.size(), "wrong line count");
        Assertions.assertNotEquals(0, lines.get(0).getAdditionalLines().size(), "additional lines missing");
        Assertions.assertEquals("java.lang.RuntimeException: Exception Message", lines.get(0).getAdditionalLines().get(0), "wrong first additional line");
    }


    @Test
    void testGetLines() throws Exception {
        TestLogWriter logWriter = new TestLogWriter(null, 0L, null);
        logWriter.writeRandomLogLine();
        logWriter.writeRandomLogLine();
        logWriter.writeRandomLogLine();
        logWriter.writeRandomLogLine();
        logWriter.writeRandomLogLine();
        logWriter.stop();
        LogReader logReader = new LogReader(new LineParserTextImpl(), LOGFILE, TextColor.ANSI.DEFAULT, WORKER_POOL, TIMER_POOL, LOG_DISPATCH_EXECUTOR);

        await(() -> logReader.getAllLines().size() == 5, 2_000L);
        Assertions.assertEquals(0, logReader.getLines(5, 3).size(), "completely out of bounds index");
        Assertions.assertEquals(1, logReader.getLines(4, 2).size(), "partially out of bounds index");
        Assertions.assertEquals(2, logReader.getLines(3, 2).size(), "at bounds index");
        Assertions.assertEquals(3, logReader.getLines(2, 3).size(), "at bounds index");
        Assertions.assertEquals(2, logReader.getLines(2, 2).size(), "in bounds index");
    }


    private void await(BooleanSupplier condition, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline && !condition.getAsBoolean()) {
            Thread.sleep(10L);
        }
    }
}