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

package de.logrifle;

import com.googlecode.lanterna.TextColor;
import de.logrifle.base.LogDispatcher;
import de.logrifle.base.RateLimiter;
import de.logrifle.base.RateLimiterFactory;
import de.logrifle.data.parsing.Line;
import de.logrifle.data.parsing.LineParseResult;
import de.logrifle.data.parsing.LineParser;
import de.logrifle.data.views.DataView;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class LogReader extends DataView {
    private final List<Line> lines = new ArrayList<>();
    private final LineParser lineParser;
    private final Tailer tailer;
    private final RateLimiter dispatcher;
    private int currentLineIndex = 0;

    LogReader(LineParser lineParser, Path logfile, TextColor fileColor, ExecutorService workerPool, LogDispatcher logDispatcher, RateLimiterFactory factory) throws IOException {
        super(logfile.getFileName().toString(), fileColor, logDispatcher, logfile.getFileName().toString().length());
        this.dispatcher = factory.newRateLimiter(this::fireUpdated, logDispatcher);
        this.lineParser = lineParser;
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
                LineParseResult parseResult = LogReader.this.lineParser.parse(currentLineIndex, s, LogReader.this);
                if (parseResult.isNewLine()) {
                    lines.add(parseResult.getParsedLine());
	                currentLineIndex++;
                } else {
                    Line last;
                    if (lines.isEmpty()) {
                        last = Line.initialTextLineOf(currentLineIndex, s, LogReader.this);
                        lines.add(last);
	                    currentLineIndex++;
                    } else {
                        last = lines.get(lines.size() - 1);
                    }
                    last.appendAdditionalLine(parseResult.getText());
                }
                dispatcher.requestExecution();
            }

            @Override
            public void handle(Exception ex) {
                super.handle(ex);
                ex.printStackTrace();
            }
        };
        tailer = new Tailer(logfile.toFile(), StandardCharsets.UTF_8, tailerListener, 250, false, false, 4096);
        workerPool.submit(tailer);
    }

    /**
     *   use only in tests!
     */
    List<Line> getLines() {
        return lines;
    }

    @Override
    public List<Line> getAllLines() {
        return new ArrayList<>(this.lines);
    }

    @Override
    public void onUpdated(DataView parent) {
        // ignored - this should never happen
    }

    @Override
    public void destroy() {
        this.shutdown();
    }

    public void shutdown() {
        tailer.stop();
    }

    @Override
    protected void clearCacheImpl() {
    }
}
