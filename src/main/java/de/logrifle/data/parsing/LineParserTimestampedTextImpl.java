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

package de.logrifle.data.parsing;

import de.logrifle.data.views.LineSource;

public class LineParserTimestampedTextImpl extends LineParser {
    private final TimeStampParser timeStampParser;

    public LineParserTimestampedTextImpl() {
        this(new TimeStampFormat(null, null));
    }

    public LineParserTimestampedTextImpl(TimeStampFormat timeStampFormat) {
        this.timeStampParser = new TimeStampParser(timeStampFormat);
    }

    @Override
    public LineParseResult parse(int index, String raw, LineSource source) {
        return this.timeStampParser.parse(raw)
                .map(timestamp -> {
                    long dateChangeCount = updateAndGetDateChangeCount(timestamp);
                    return new LineParseResult(new Line(index, raw, timestamp, dateChangeCount, source));
                })
                .orElseGet(() -> new LineParseResult(raw));
    }
}
