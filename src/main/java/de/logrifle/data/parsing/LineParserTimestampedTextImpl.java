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

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LineParserTimestampedTextImpl implements LineParser {
    private final Pattern timeStampPattern;
    private final DateTimeFormatter dateFormatter;
    private final Function<String, Long> timeParser;
    private static final List<String> DATE_FORMAT_SPECIFIERS = Arrays.asList("G", "y", "M", "w", "W", "D", "d", "F", "E");

    public LineParserTimestampedTextImpl() {
        this(new TimeStampFormat(null, null));
    }

    public LineParserTimestampedTextImpl(TimeStampFormat timeStampFormat) {
        this.timeStampPattern = Pattern.compile(timeStampFormat.getRegex());
        this.dateFormatter = DateTimeFormatter.ofPattern(timeStampFormat.getFormat());
        if (isDateFormatter(timeStampFormat.getFormat())) {
            timeParser = this::parseDate;
        } else {
            timeParser = this::parseTime;
        }
    }

    private static boolean isDateFormatter(String format) {
        for (String dateFormatSpecifier : DATE_FORMAT_SPECIFIERS) {
            if (format.contains(dateFormatSpecifier)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public LineParseResult parse(int index, String raw, LineSource source) {
        long timestamp;
        Matcher matcher = timeStampPattern.matcher(raw);
        if (matcher.find()) {
            String dateString = matcher.group(1);
            try {
                timestamp = timeParser.apply(dateString);
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

    private long parseDate(String input) {
        LocalDateTime parsed = LocalDateTime.parse(input, dateFormatter);
        ZonedDateTime zdt = ZonedDateTime.of(parsed, ZoneId.of("UTC"));
        return zdt.toInstant().toEpochMilli();
    }

    private long parseTime(String input) {
        LocalTime parsed = LocalTime.parse(input, dateFormatter);
        return TimeUnit.NANOSECONDS.toMillis(parsed.toNanoOfDay());
    }
}
