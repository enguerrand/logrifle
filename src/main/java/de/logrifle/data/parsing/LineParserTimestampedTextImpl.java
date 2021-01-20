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
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LineParserTimestampedTextImpl implements LineParser {
    private final Pattern timeStampPattern;
    private final DateTimeFormatter dateFormatter;
    private final Function<String, Long> timeParser;
    private final boolean dayInFormat;
    private final boolean monthInFormat;
    private final boolean yearInFormat;
    private final String datePrefix;

    public LineParserTimestampedTextImpl() {
        this(new TimeStampFormat(null, null));
    }

    public LineParserTimestampedTextImpl(TimeStampFormat timeStampFormat) {
        this.timeStampPattern = Pattern.compile(timeStampFormat.getRegex());
        String format = timeStampFormat.getFormat();
        this.dayInFormat = containsDay(format);
        this.monthInFormat = containsMonth(format);
        this.yearInFormat = containsYear(format);
        StringBuilder prefixBuilder = new StringBuilder();
        StringBuilder formatBuilder = new StringBuilder();
        if (this.dayInFormat) {
            timeParser = this::parseDate;
            if (!yearInFormat) {
                formatBuilder.append("yyyy ");
                prefixBuilder.append("1970 ");
                if (!monthInFormat) {
                    formatBuilder.append("MM ");
                    prefixBuilder.append("01 ");
                }
            }
            formatBuilder.append(format);
            dateFormatter = DateTimeFormatter.ofPattern(formatBuilder.toString());
            datePrefix = prefixBuilder.toString();
        } else {
            timeParser = this::parseTime;
            dateFormatter = DateTimeFormatter.ofPattern(format);
            datePrefix = "";
        }
    }

    @Override
    public LineParseResult parse(int index, String raw, LineSource source) {
        long timestamp;
        Matcher matcher = timeStampPattern.matcher(raw);
        if (matcher.find()) {
            String dateString = matcher.group(1);
            try {
                timestamp = timeParser.apply(dateString);
                return new LineParseResult(new Line(index, raw, timestamp, source));
            } catch (RuntimeException ignored) {
                /*
                 In rare cases this can legitimately happen e.g. for an unluckily logged MAC address such as AB:CD:EF:12:34:56
                 (which looks like timestamp 12:34:56)
                 This could arguably be avoided by tighter regex choice in many if not all cases but it seems more
                 user-friendly to gracefully handle this case gracefully.
                 */
            }
        }
        return new LineParseResult(raw);
    }

    private long parseDate(String input) {
        String pimpedInput = datePrefix + input;
        LocalDateTime parsed = LocalDateTime.parse(pimpedInput, dateFormatter);
        ZonedDateTime zdt = ZonedDateTime.of(parsed, ZoneId.of("UTC"));
        return zdt.toInstant().toEpochMilli();
    }

    private long parseTime(String input) {
        LocalTime parsed = LocalTime.parse(input, dateFormatter);
        return TimeUnit.NANOSECONDS.toMillis(parsed.toNanoOfDay());
    }

    private static boolean containsYear(String format) {
        return format.contains("yy");
    }

    private static boolean containsMonth(String format) {
        return format.contains("M");
    }

    private static boolean containsDay(String format) {
        return format.contains("d");
    }
}
