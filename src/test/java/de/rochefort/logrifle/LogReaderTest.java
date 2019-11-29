package de.rochefort.logrifle;

import de.rochefort.logrifle.data.Line;
import de.rochefort.logrifle.data.LineParserTextImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;

class LogReaderTest {
    static Path LOGFILE = Paths.get("./out/log.log");

    @BeforeEach
    void setUp() throws IOException {
        if(Files.exists(LOGFILE)){
            Files.write(LOGFILE, new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    @AfterAll
    static void tearDown() throws IOException {
        Files.delete(LOGFILE);
    }

    @Test
    void testReadStaticLog() throws Exception {
        TestLogWriter logWriter = new TestLogWriter(null, 0L);
        logWriter.writeRandomLogLine();
        logWriter.writeRandomLogLine();
        logWriter.writeRandomLogLine();
        logWriter.stop();
        LogReader logReader = new LogReader(new LineParserTextImpl(), LOGFILE);
        List<Line> lines = logReader.getLines();
        Assertions.assertEquals(3, lines.size(), "wrong line count");
        Assertions.assertTrue(lines.get(1).getRaw().endsWith("ullam doloremque quia dolorem pariatur. adipiscing 0076.32 nesciunt. dolore"), "Wrong line content");
    }

    @Test
    void testOpenDynamicLog() throws Exception {
        TestLogWriter logWriter = new TestLogWriter(null, 0L);
        logWriter.writeRandomLines(100);
        CompletableFuture<Void> f = logWriter.start(100, 200);
        LogReader logReader = new LogReader(new LineParserTextImpl(), LOGFILE);
        f.get();
        List<Line> lines = logReader.getLines();
        await(() -> lines.size() == 300, 10_000L);
        Assertions.assertEquals(300, lines.size(), "wrong line count");
        Assertions.assertTrue(lines.get(1).getRaw().endsWith("ullam doloremque quia dolorem pariatur. adipiscing 0076.32 nesciunt. dolore"), "Wrong line content");
        Assertions.assertTrue(lines.get(99).getRaw().endsWith("laboriosam, autem minima ut est ad qui veritatis sunt dolore in sit quae labore öäßaweawe( consequat."), "Wrong line content");
        Assertions.assertTrue(lines.get(100).getRaw().endsWith("aperiam, pariatur. veniam,"), "Wrong line content");
        Assertions.assertTrue(lines.get(299).getRaw().endsWith("eum qui nostrud ut"), "Wrong line content");
    }

    private void await(BooleanSupplier condition, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline && !condition.getAsBoolean()) {
            Thread.sleep(10L);
        }
    }
}