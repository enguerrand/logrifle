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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TimeStampFormats {
    public static final String MILLIS_TIME_MATCH_REGEX = ".*\\b(\\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\b.*";
    public static final String MILLIS_DATE_FORMAT = "HH:mm:ss.SSS";
    public static final String SECONDS_TIME_MATCH_REGEX = ".*\\b(\\d{2}:\\d{2}:\\d{2})\\b.*";
    public static final String SECONDS_DATE_FORMAT = "HH:mm:ss";
    public static final String ISO_DATE_TIME_MATCH_REGEX = ".*([2-9]\\d{3}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}).*";
    public static final String ISO_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public static final TimeStampFormat FORMAT_MILLIS = new TimeStampFormat(MILLIS_TIME_MATCH_REGEX, MILLIS_DATE_FORMAT);
    public static final TimeStampFormat FORMAT_SECONDS = new TimeStampFormat(SECONDS_TIME_MATCH_REGEX, SECONDS_DATE_FORMAT);
    public static final TimeStampFormat FORMAT_ISO_DATE_TIME = new TimeStampFormat(ISO_DATE_TIME_MATCH_REGEX, ISO_DATE_TIME_FORMAT);
    public static final Integer DEFAULT_AUTO_DETECTION_LINE_COUNT = 100;


    public static final List<TimeStampFormat> DEFAULT_AUTO_DETECT_CANDIDATES = Arrays.asList(
            FORMAT_ISO_DATE_TIME,
            FORMAT_MILLIS,
            FORMAT_SECONDS
    );

    private final List<TimeStampFormatTester> formatCandidates;

    public TimeStampFormats() {
       this(DEFAULT_AUTO_DETECT_CANDIDATES);
    }

    public TimeStampFormats(List<TimeStampFormat> formatCandidates) {
        this.formatCandidates = formatCandidates.stream()
                .map(TimeStampFormatTester::new)
                .collect(Collectors.toList());
    }

    public Collection<TimeStampFormat> getMatchingTimestampFormats(String line) {
        return formatCandidates.stream()
                .filter(t -> t.test(line))
                .map(TimeStampFormatTester::getFormat)
                .collect(Collectors.toList());
    }

    public Optional<TimeStampFormat> autoDetectFormat(Collection<String> input) {
        Map<TimeStampFormat, Long> matchCounts = new HashMap<>();
        for (String line : input) {
            Collection<TimeStampFormat> matchingTimestampFormats = getMatchingTimestampFormats(line);
            for (TimeStampFormat matchingTimestampFormat : matchingTimestampFormats) {
                matchCounts.compute(matchingTimestampFormat, (key, prevValue) -> {
                    if (prevValue == null) {
                        return 1L;
                    } else {
                        return prevValue + 1;
                    }
                });
            }
        }

        double matchThreshold = 0.8;
        List<TimeStampFormat> aboveMatchThreshold = Collections.emptyList();
        while (aboveMatchThreshold.isEmpty() && matchThreshold > 0.2) {
            matchThreshold -= 0.1;
            aboveMatchThreshold = findMatchesForThreshold(input, matchCounts, matchThreshold);
        }
        List<TimeStampFormat> formats = formatCandidates.stream()
                .map(TimeStampFormatTester::getFormat)
                .collect(Collectors.toList());
        return aboveMatchThreshold.stream()
                .min(Comparator.comparingInt(formats::indexOf))
        ;
    }

    private List<TimeStampFormat> findMatchesForThreshold(Collection<String> input, Map<TimeStampFormat, Long> matchCounts, double matchThreshold) {
        return matchCounts.entrySet()
                .stream()
                .filter(entry -> {
                    Long count = entry.getValue();
                    return count >= input.size() * matchThreshold;
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private static final class TimeStampFormatTester {
        private final TimeStampFormat format;
        private final TimeStampParser parser;

        private TimeStampFormatTester(TimeStampFormat format) {
            this.format = format;
            this.parser = new TimeStampParser(format);
        }

        private boolean test(String input) {
            return parser.parse(input).isPresent();
        }

        private TimeStampFormat getFormat() {
            return format;
        }
    }
}
