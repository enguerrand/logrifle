package de.rochefort.logrifle;

import de.rochefort.logrifle.data.Line;
import de.rochefort.logrifle.data.LineParser;
import de.rochefort.logrifle.data.LineParserTextImpl;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LogReader {
    private volatile List<Line> lines = null;
    private final LineParser lineParser;

    public LogReader(LineParser lineParser, Path logfile) throws IOException {
        this.lineParser = lineParser;
        final List<Line> tailBuffer = new ArrayList<>();
        TailerListener tailerListener = new TailerListenerAdapter() {
            @Override
            public void init(Tailer tailer) {
                super.init(tailer);
            }

            /**
             * this implementation is not thread safe. the assumption is that it will only be called on the Tailer Thread
             */
            @Override
            public void handle(String s) {
                Line line = LogReader.this.lineParser.parse(s);
                if (lines != null) {
                    if (!tailBuffer.isEmpty()) {
                        readBuffer(tailBuffer);
                        tailBuffer.clear();
                    }
                    lines.add(line);
                } else {
                    tailBuffer.add(line);
                }
            }

            @Override
            public void handle(Exception ex) {
                super.handle(ex);
                ex.printStackTrace();
            }
        };
        Tailer tailer = new Tailer(logfile.toFile(), tailerListener, 250, true);
        Thread thread = new Thread(tailer);
        thread.setDaemon(false);
        thread.start();

        this.lines = Files.readAllLines(logfile, StandardCharsets.UTF_8)
                .stream()
                .map(this.lineParser::parse)
                .collect(Collectors.toList());
    }

    private void readBuffer(List<Line> tailBuffer) {
        boolean newLineFound = false;
        for (Line bufferedLine : tailBuffer) {
            if (!newLineFound) {
                if (lines.contains(bufferedLine)) {
                    continue;
                }
                newLineFound = true;
            }
            lines.add(bufferedLine);
        }
    }

    public List<Line> getLines() {
        return lines;
    }

    public static void main(String[] args) throws IOException {
        long begin = System.nanoTime();
        if (args.length == 0) {
            System.err.println("Need path to file!");
            return;
        }
        String pathToLogFile = args[0];
        Path path = Paths.get(pathToLogFile);
        LogReader logReader = new LogReader(new LineParserTextImpl(), path);
        long end = System.nanoTime();
        System.out.println("read "+logReader.getLines().size() +" in "+ TimeUnit.NANOSECONDS.toMillis(end-begin) +"ms");
    }
}
