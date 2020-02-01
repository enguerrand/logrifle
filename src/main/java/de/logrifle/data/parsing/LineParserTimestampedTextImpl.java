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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LineParserTimestampedTextImpl implements LineParser {
    private final Pattern timeStampPattern;
    private final DateTimeFormatter dateFormatter;

    public LineParserTimestampedTextImpl() {
        this(new TimeStampFormat(null, null));
    }

    public LineParserTimestampedTextImpl(TimeStampFormat timeStampFormat) {
        this.timeStampPattern = Pattern.compile(timeStampFormat.getRegex());
        this.dateFormatter = DateTimeFormatter.ofPattern(timeStampFormat.getFormat());
    }

    @Override
    public LineParseResult parse(int index, String raw, LineSource source) {
        long timestamp;
        Matcher matcher = timeStampPattern.matcher(raw);
        if (matcher.find()) {
            String dateString = matcher.group(1);
            LocalTime parsed = null;
            try {
                parsed = LocalTime.parse(dateString, dateFormatter);
                timestamp = TimeUnit.NANOSECONDS.toMillis(parsed.toNanoOfDay());
            } catch (RuntimeException e) {
                throw new IllegalStateException("Error while parsing datestring. \""+dateString+"\"." +
                        "The date string pattern matches but the matched string cannot be parsed with the given date format! " +
                        "Complete log line: \""+raw+"\"", e);
            }
        } else {
            return new LineParseResult(raw);
        }
        return new LineParseResult(new Line(index, raw, timestamp, source));
    }
}
