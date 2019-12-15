/*
 *  Copyright 2019, Enguerrand de Rochefort
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

package de.rochefort.logrifle;

import de.rochefort.logrifle.base.LogDispatcher;
import de.rochefort.logrifle.base.RateLimiter;
import de.rochefort.logrifle.data.parsing.Line;
import de.rochefort.logrifle.data.parsing.LineParseResult;
import de.rochefort.logrifle.data.parsing.LineParser;
import de.rochefort.logrifle.data.views.DataView;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

public class LogReader extends DataView {
    private volatile List<Line> lines = null;
    private final LineParser lineParser;
    private final Tailer tailer;
    private final RateLimiter dispatcher;

    LogReader(LineParser lineParser, Path logfile, ExecutorService workerPool, ScheduledExecutorService timerPool, LogDispatcher logDispatcher) throws IOException {
        super(logfile.getFileName().toString(), logDispatcher, logfile.getFileName().toString().length());
        this.dispatcher = new RateLimiter(this::fireUpdated, logDispatcher, timerPool, 150);
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
                LineParseResult parseResult = LogReader.this.lineParser.parse(s, getTitle());
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
                dispatcher.requestExecution();
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
        tailer = new Tailer(logfile.toFile(), tailerListener, 250, true);
        workerPool.submit(tailer);

        List<Line> readResult = new ArrayList<>();
        Line lastLine = null;
        for (String current : Files.readAllLines(logfile, StandardCharsets.UTF_8)) {
            LineParseResult parseResult = this.lineParser.parse(current, getTitle());
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

    /**
     *   use only in tests!
     */
    List<Line> getLines() {
        return lines;
    }

    @Override
    public int getLineCount() {
        List<Line> snapshot = this.lines;
        return snapshot == null ? 0 : snapshot.size();
    }

    @Override
    public List<Line> getAllLines() {
        return new ArrayList<>(this.lines);
    }

    @Override
    public void onUpdated(DataView parent) {
        // ignored - this should never happen
    }

    public void shutdown() {
        tailer.stop();
    }
}
