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

package de.rochefort.logrifle.data.parsing;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LineParserTimestampedTextImpl implements LineParser {
    public static final String DEFAULT_TIME_MATCH_REGEX = ".*(\\d{2}:\\d{2}:\\d{2}\\.\\d{3}).*";
    public static final String DEFAULT_DATE_FORMAT = "HH:mm:ss.SSS";
    private final Pattern timeStampPattern;
    private final DateFormat dateFormat;

    public LineParserTimestampedTextImpl() {
        this(null, null);
    }

    public LineParserTimestampedTextImpl(String timestampRegex, String dateFormat) {
        this.timeStampPattern = Pattern.compile(timestampRegex != null ? timestampRegex : DEFAULT_TIME_MATCH_REGEX);
        this.dateFormat = new SimpleDateFormat(dateFormat != null ? dateFormat : DEFAULT_DATE_FORMAT);
    }

    @Override
    public LineParseResult parse(String raw) {
        long timestamp;
        Matcher matcher = timeStampPattern.matcher(raw);
        if (matcher.find()) {
            String dateString = matcher.group(1);
            Date parsed = null;
            try {
                parsed = this.dateFormat.parse(dateString);
                timestamp = parsed.getTime();
            } catch (ParseException e) {
                throw new IllegalStateException("Error while parsing datestring. \""+dateString+"\"." +
                        "The date string pattern matches but the matched string cannot be parsed with the given date format! " +
                        "Complete log line: \""+raw+"\"", e);
            }
        } else {
            return new LineParseResult(raw);
        }
        return new LineParseResult(new Line(raw, timestamp));
    }
}
