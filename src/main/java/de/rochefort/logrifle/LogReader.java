package de.rochefort.logrifle;

import de.rochefort.logrifle.data.parsing.Line;
import de.rochefort.logrifle.data.parsing.LineParseResult;
import de.rochefort.logrifle.data.parsing.LineParser;
import de.rochefort.logrifle.data.parsing.LineParserTimestampedTextImpl;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LogReader {
    private volatile List<Line> lines = null;
    private final LineParser lineParser;

    public LogReader(LineParser lineParser, Path logfile) throws IOException {
        this.lineParser = lineParser;
        final List<Line> tailBuffer = new ArrayList<>();
        TailerListener tailerListener = new TailerListenerAdapter() {
            private @Nullable Line lastLine;
            @Override
            public void init(Tailer tailer) {
                super.init(tailer);
            }

            /**
             * this implementation is not thread safe. the assumption is that it will only be called on the Tailer Thread
             */
            @Override
            public void handle(String s) {
                LineParseResult parseResult = LogReader.this.lineParser.parse(s);
                if (lines != null) {
                    handleDirect(parseResult);
                } else {
                    handleBuffered(parseResult);
                }
            }

            private void handleDirect(LineParseResult parseResult) {
                if (!tailBuffer.isEmpty()) {
                    readBuffer(tailBuffer);
                    tailBuffer.clear();
                }
                if (parseResult.isNewLine()) {
                    lines.add(parseResult.getParsedLine());
                } else {
                    Line last = lines.get(lines.size() - 1);
                    last.appendAdditionalLine(parseResult.getText());
                }
            }

            private void handleBuffered(LineParseResult parseResult) {
                if (parseResult.isNewLine()) {
                    this.lastLine = parseResult.getParsedLine();
                    tailBuffer.add(this.lastLine);
                } else {
                    if (this.lastLine != null) {
                        this.lastLine.appendAdditionalLine(parseResult.getText());
                    }
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

        List<Line> readResult = new ArrayList<>();
        Line lastLine = null;
        for (String current : Files.readAllLines(logfile, StandardCharsets.UTF_8)) {
            LineParseResult parseResult = this.lineParser.parse(current);
            if (parseResult.isNewLine()) {
                lastLine = parseResult.getParsedLine();
                readResult.add(lastLine);
            } else if (lastLine != null) {
                lastLine.appendAdditionalLine(parseResult.getText());
            }
        }
        this.lines = readResult;
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
//        LineParser lineParser = new LineParserTextImpl();
        LineParser lineParser = new LineParserTimestampedTextImpl();
        LogReader logReader = new LogReader(lineParser, path);
        long end = System.nanoTime();
        System.out.println("read "+logReader.getLines().size() +" in "+ TimeUnit.NANOSECONDS.toMillis(end-begin) +"ms");
    }
}
