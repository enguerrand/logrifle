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
import de.logrifle.base.LogDispatcher;
import de.logrifle.data.parsing.Line;
import de.logrifle.data.parsing.LineParseResult;
import de.logrifle.data.parsing.LineParser;
import de.logrifle.data.views.DataView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LogInputStreamReader extends DataView {
    private final List<Line> lines = new ArrayList<>();

    public LogInputStreamReader(InputStream inputStream, LineParser lineParser, TextColor fileColor, LogDispatcher logDispatcher, String title) throws IOException {
        super(title, fileColor, logDispatcher, title.length());
        String raw;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        int currentLineIndex = 0;
        while ((raw = reader.readLine()) != null) {
            LineParseResult parseResult = lineParser.parse(currentLineIndex++, raw, this);
            if (parseResult.isNewLine()) {
                lines.add(
                        Objects.requireNonNull(
                                parseResult.getParsedLine(),
                                () -> "Unexpected NULL Line received from LogInputStreamReader parseResult " + parseResult
                        )
                );
                currentLineIndex++;
            } else {
                Line last;
                if (lines.isEmpty()) {
                    Line initialTextLine = Line.initialTextLineOf(currentLineIndex, raw, this);
                    last = Objects.requireNonNull(
                            initialTextLine,
                            () -> "Unexpected NULL Line received from initialTextLineOf call in LogInputStreamReader on parseResult " + parseResult
                    );
                    lines.add(last);
                    currentLineIndex++;
                } else {
                    last = lines.get(lines.size() - 1);
                }
                last.appendAdditionalLine(parseResult.getText());
            }
        }
        logDispatcher.execute(this::fireUpdated);
    }

    @Override
    public List<Line> getAllLines() {
        return new ArrayList<>(lines);
    }

    @Override
    protected void clearCacheImpl() {
        fireCacheCleared();
    }

    @Override
    public void onFullUpdate(DataView source) {
        // this should never happen
    }

    @Override
    public void onIncrementalUpdate(DataView source, List<Line> newLines) {
        // this should never happen
    }

    @Override
    public void onDestroyed(DataView source) {
        if (this.equals(source)) {
            this.lines.clear();
        }
        super.onDestroyed(source);
    }
}
