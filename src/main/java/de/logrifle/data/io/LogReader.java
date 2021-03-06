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

package de.logrifle.data.io;

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

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

public class LogReader extends DataView {
    private final List<Line> lines = new ArrayList<>();
    private final List<Line> linesSnapshot = new CopyOnWriteArrayList<>();
    private final LineParser lineParser;
    private final Tailer tailer;
    private final RateLimiter dispatcher;
    private int currentLineIndex = 0;

    LogReader(LineParser lineParser, Path logfile, TextColor fileColor, ExecutorService workerPool, LogDispatcher logDispatcher, RateLimiterFactory factory, Charset charset) {
        super(logfile.getFileName().toString(), fileColor, logDispatcher, logfile.getFileName().toString().length());
        this.dispatcher = factory.newRateLimiter(this::fireUpdatedInternal, logDispatcher);
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
                    lines.add(
                            Objects.requireNonNull(
                                    parseResult.getParsedLine(),
                                    () -> "Unexpected NULL Line received from LogReader parseResult " + parseResult
                            )
                    );
	                currentLineIndex++;
                } else {
                    Line last;
                    if (lines.isEmpty()) {
                        Line initialTextLine = Line.initialTextLineOf(currentLineIndex, s, LogReader.this);
                        last = Objects.requireNonNull(
                                initialTextLine,
                                () -> "Unexpected NULL Line received from initialTextLineOf call in LogReader on parseResult " + parseResult
                        );
                        lines.add(last);
	                    currentLineIndex++;
                    } else {
                        last = lines.get(lines.size() - 1);
                    }
                    last.appendAdditionalLine(parseResult.getText());
                }
                dispatcher.requestExecution();
            }
        };
        tailer = new Tailer(logfile.toFile(), charset, tailerListener, 250, false, false, 4096);
        workerPool.submit(tailer);
    }

    /**
     *   use only in tests!
     */
    List<Line> getLines() {
        return linesSnapshot;
    }

    private void fireUpdatedInternal() {
        ArrayList<Line> snapshot = new ArrayList<>(this.lines);
        if (this.linesSnapshot.isEmpty()) {
            linesSnapshot.addAll(snapshot);
            fireUpdated();
        } else {
            List<Line> newLines = snapshot.subList(linesSnapshot.size(), snapshot.size());
            linesSnapshot.addAll(newLines);
            fireUpdatedIncremental(newLines);
        }
    }

    @Override
    public List<Line> getAllLines() {
        return new ArrayList<>(this.linesSnapshot);
    }

    @Override
    public void onLineVisibilityStateInvalidated(Collection<Line> invalidatedLines, DataView source) {
        // ignored - this should never happen
    }

    @Override
    public void onFullUpdate(DataView parent) {
        // ignored - this should never happen
    }

    @Override
    public void onIncrementalUpdate(DataView source, List<Line> newLines) {
        // ignored - this should never happen
    }

    @Override
    public void onDestroyed(DataView source) {
        if (this.equals(source)) {
            this.shutdown();
        }
        super.onDestroyed(source);
        this.lines.clear();
    }

    public void shutdown() {
        tailer.stop();
    }

    @Override
    protected void clearCacheImpl() {
        this.getLogDispatcher().execute(this.linesSnapshot::clear);
    }
}
