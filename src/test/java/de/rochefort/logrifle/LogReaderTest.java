package de.rochefort.logrifle;

import de.rochefort.logrifle.data.Line;
import de.rochefort.logrifle.data.LineParserTextImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

class LogReaderTest {
    Path logfile = Paths.get("./out/log.log");

    @BeforeEach
    void setUp() throws IOException {
        Files.deleteIfExists(logfile);
    }

    @AfterEach
    void tearDown() throws IOException {
//        Files.deleteIfExists(logfile);
    }

    @Test
    void testReadStaticLog() throws IOException {
        TestLogWriter logWriter = new TestLogWriter(null, 0L);
        logWriter.writeRandomLogLine();
        logWriter.writeRandomLogLine();
        logWriter.writeRandomLogLine();
        LogReader logReader = new LogReader(new LineParserTextImpl(), logfile);
        List<Line> lines = logReader.getLines();
        Assertions.assertEquals(3, lines.size(), "wrong line count");
        Assertions.assertTrue(lines.get(1).getRaw().endsWith("ullam doloremque quia dolorem pariatur. adipiscing 0076.32 nesciunt. dolore"), "Wrong line content");
    }

    @Test
    void testOpenDynamicLog() throws IOException, InterruptedException, ExecutionException {
        TestLogWriter logWriter = new TestLogWriter(null, 0L);
        logWriter.writeRandomLines(100);
        CompletableFuture<Void> f = logWriter.start(100, 200);
        LogReader logReader = new LogReader(new LineParserTextImpl(), logfile);
        f.get();
        List<Line> lines = logReader.getLines();

        Assertions.assertEquals(300, lines.size(), "wrong line count");
        Assertions.assertTrue(lines.get(1).getRaw().endsWith("ullam doloremque quia dolorem pariatur. adipiscing 0076.32 nesciunt. dolore"), "Wrong line content");
        Assertions.assertTrue(lines.get(99).getRaw().endsWith("laboriosam, autem minima ut est ad qui veritatis sunt dolore in sit quae labore öäßaweawe( consequat."), "Wrong line content");
        Assertions.assertTrue(lines.get(100).getRaw().endsWith("aperiam, pariatur. veniam,"), "Wrong line content");
        Assertions.assertTrue(lines.get(299).getRaw().endsWith("eum qui nostrud ut"), "Wrong line content");
    }
}