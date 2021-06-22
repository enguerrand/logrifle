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

package de.logrifle.data.parsing;

import de.logrifle.data.views.LineSourceTestImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

class LineParserTimestampedTextImplTest {

    @BeforeAll
    static void setUp() {
        Locale.setDefault(new Locale("en", "US"));
    }


    @Test
    void testDateChangeCount() {
        LineParserTimestampedTextImpl parser = new LineParserTimestampedTextImpl();
        LineSourceTestImpl source = new LineSourceTestImpl("foo");
        int index = 0;

        Assertions.assertEquals(0, parser.parse(index++, "21:04:17.394 foobar", source).getParsedLine().getDateChangeCount());
        Assertions.assertEquals(0, parser.parse(index++, "21:05:17.394 foobar", source).getParsedLine().getDateChangeCount());
        Assertions.assertEquals(1, parser.parse(index++, "19:05:17.394 foobar", source).getParsedLine().getDateChangeCount());
        Assertions.assertEquals(1, parser.parse(index++, "19:05:19.394 foobar", source).getParsedLine().getDateChangeCount());
        Assertions.assertEquals(1, parser.parse(index++, "19:05:19.395 foobar", source).getParsedLine().getDateChangeCount());
        Assertions.assertEquals(1, parser.parse(index++, "19:05:14.395 foobar", source).getParsedLine().getDateChangeCount()); // up to negative 5 secs should be considered a race condition in the log file
        Assertions.assertEquals(1, parser.parse(index, "19:05:19.397 foobar", source).getParsedLine().getDateChangeCount());
    }

    @ParameterizedTest
    @MethodSource("getTestCases")
    void parse(TestCase testCase) {
        LineParserTimestampedTextImpl parser = new LineParserTimestampedTextImpl(testCase.timeStampFormat);
        LineParseResult parseResult = parser.parse(0, testCase.raw, new LineSourceTestImpl("foo"));
        Line line = parseResult.getParsedLine();
        Assertions.assertTrue(parseResult.isNewLine(), "newLine should be true");
        Assertions.assertEquals(testCase.raw, line.getRaw(), "Wrong raw content");
        Assertions.assertEquals(testCase.expectedTimestamp, line.getTimestamp(), "wrong timestamp");
    }

    private static List<TestCase> getTestCases() {
        return Arrays.asList(
            new TestCase("21:04:17.394 foobar", 75857394L, ".*(\\d{2}:\\d{2}:\\d{2}\\.\\d{3}).*", "HH:mm:ss.SSS"),
            new TestCase("21:04:17.394", 75857394L, "(.*)", "HH:mm:ss.SSS"),
            new TestCase("1970-01-01 21:04:17.394", 75857394L, "(.*)", "yyyy-MM-dd HH:mm:ss.SSS"),
            new TestCase("1970 Jan 01 21:04:17", 75857000L, "(.*)", "yyyy MMM dd HH:mm:ss"),
            new TestCase("2023 12 11 21:04:17", 1702328657000L, "(.*)", "yyyy MM dd HH:mm:ss"),
            new TestCase("Jan 01 21:04:17", 75857000L, "(.*)", "MMM dd HH:mm:ss"),
            new TestCase("Mar 12 21:04:17", 6123857000L, "(.*)", "MMM dd HH:mm:ss")
        );
    }

    private static class TestCase {
        final String raw;
        final long expectedTimestamp;
        final TimeStampFormat timeStampFormat;

        private TestCase(String raw, long expectedTimestamp, String regex, String format) {
            this.raw = raw;
            this.expectedTimestamp = expectedTimestamp;
            timeStampFormat = new TimeStampFormat(regex, format);
        }
    }

    private static class LineAndDateChangeCount {
        final String raw;
        final int expectedChangeCount;

        private LineAndDateChangeCount(String raw, int expectedChangeCount) {
            this.raw = raw;
            this.expectedChangeCount = expectedChangeCount;
        }
    }
}