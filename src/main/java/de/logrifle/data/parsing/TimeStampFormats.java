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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class TimeStampFormats {
    public static final String DEFAULT_TIME_MATCH_REGEX = ".*\\b(\\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\b.*";
    public static final String DEFAULT_DATE_FORMAT = "HH:mm:ss.SSS";
    public static final String SECONDS_TIME_MATCH_REGEX = ".*\\b(\\d{2}:\\d{2}:\\d{2})\\b.*";
    public static final String SECONDS_DATE_FORMAT = "HH:mm:ss";
    public static final TimeStampFormat FORMAT_MILLIS = new TimeStampFormat(DEFAULT_TIME_MATCH_REGEX, DEFAULT_DATE_FORMAT);
    public static final TimeStampFormat FORMAT_SECONDS = new TimeStampFormat(SECONDS_TIME_MATCH_REGEX, SECONDS_DATE_FORMAT);


    public static final List<TimeStampFormat> DEFAULT_AUTO_DETECT_CANDIDATES = Arrays.asList(
            FORMAT_MILLIS,
            FORMAT_SECONDS
    );

    private final List<TimeStampFormatTester> formatCandidates;

    public TimeStampFormats() {
       this(DEFAULT_AUTO_DETECT_CANDIDATES);
    }

    public TimeStampFormats(List<TimeStampFormat> formatCandidates) {
        List<TimeStampFormatTester> list = new ArrayList<>();
        for (int index = 0, formatCandidatesSize = formatCandidates.size(); index < formatCandidatesSize; index++) {
            TimeStampFormat format = formatCandidates.get(index);
            TimeStampFormatTester timeStampFormatTester = new TimeStampFormatTester(format, index);
            list.add(timeStampFormatTester);
        }
        this.formatCandidates = list;
    }

    public Collection<TimeStampFormat> getMatchingTimestampFormats(String line) {
        return formatCandidates.stream()
                .filter(t -> t.test(line))
                .map(TimeStampFormatTester::getFormat)
                .collect(Collectors.toList());
    }

    private static final class TimeStampFormatTester {
        private final TimeStampFormat format;
        private final TimeStampParser parser;
        private final int order;

        private TimeStampFormatTester(TimeStampFormat format, int order) {
            this.format = format;
            this.parser = new TimeStampParser(format);
            this.order = order;
        }

        private boolean test(String input) {
            return parser.parse(input).isPresent();
        }

        private TimeStampFormat getFormat() {
            return format;
        }
    }
}
