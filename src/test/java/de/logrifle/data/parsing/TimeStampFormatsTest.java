/*
 *  Copyright 2021, Enguerrand de Rochefort
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

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.logrifle.data.parsing.TimeStampFormats.FORMAT_MILLIS;
import static de.logrifle.data.parsing.TimeStampFormats.FORMAT_SECONDS;

class TimeStampFormatsTest {
    private List<TimeStampFormat> candidates = Arrays.asList(FORMAT_MILLIS, FORMAT_SECONDS);
    private TimeStampFormats formats = new TimeStampFormats(candidates);

    private static Stream<Arguments> getMatchingFormatArgs() {
        return Stream.of(
                argsOf("21:17:04.714 aliquid unde", FORMAT_SECONDS, FORMAT_MILLIS),
                argsOf("21:17:04 aliquid unde", FORMAT_SECONDS),
                argsOf("21:17:04 21:17:04.123 aliquid unde", FORMAT_MILLIS, FORMAT_SECONDS),
                argsOf("21:17 aliquid unde"),
                argsOf("21:17:61 aliquid unde")
        );
    }

    private static Arguments argsOf(String input, TimeStampFormat... formats) {
        return Arguments.of(input, Arrays.asList(formats));
    }

    @ParameterizedTest
    @MethodSource("getMatchingFormatsArgs")
    void testMatchingFormats(String input, List<TimeStampFormat> expectedMatches) {
        Collection<TimeStampFormat> matches = formats.getMatchingTimestampFormats(input);
        Assertions.assertEquals(expectedMatches.size(), matches.size());
        Assertions.assertTrue(matches.containsAll(expectedMatches));
    }


    private static Stream<Arguments> getMatchingFormatsArgs() {
        return Stream.of(
                argsOf("21:17:04.714 aliquid unde", FORMAT_SECONDS, FORMAT_MILLIS),
                argsOf("21:17:04 aliquid unde", FORMAT_SECONDS),
                argsOf("21:17:04 21:17:04.123 aliquid unde", FORMAT_MILLIS, FORMAT_SECONDS),
                argsOf("21:17 aliquid unde"),
                argsOf("21:17:61 aliquid unde")
        );
    }

    @ParameterizedTest
    @MethodSource("getAutoDetectionArgs")
    void testAutoDetectFormat(List<String> input, @Nullable TimeStampFormat expectedAutoDetectionResult) {
        Optional<TimeStampFormat> detectionResult = formats.autoDetectFormat(input);
        Assertions.assertEquals(expectedAutoDetectionResult, detectionResult.orElse(null));
    }

    private static Stream<Arguments> getAutoDetectionArgs() {
        return Stream.of(
                Arguments.of(Arrays.asList(" 21:17:04 a", " 21:17:04.124 a"), FORMAT_MILLIS),
                Arguments.of(Arrays.asList(" 21:17:04.124 a", " 21:17:04 a"), FORMAT_MILLIS),
                Arguments.of(Arrays.asList(" 21:17:04 a", " 21:17:04.124 a", " 21:17:04 a"), FORMAT_SECONDS),
                Arguments.of(Arrays.asList(" 21:17:04 a", " 21:17:04.124 a", " 21:17:04 a", " 22:17:04.124 a"), FORMAT_MILLIS),
                Arguments.of(Arrays.asList(" 21:17:04 a", " 21:17:04.12 a"), FORMAT_SECONDS),
                Arguments.of(Arrays.asList(" 21:17:04 a", " 21:17:04 a"), FORMAT_SECONDS),
                Arguments.of(Arrays.asList(" foo a", " 21:17:04 a"), FORMAT_SECONDS),
                Arguments.of(Arrays.asList(" 21:17:04.124 a", " wups a"), FORMAT_MILLIS),
                Arguments.of(Arrays.asList(" blajh a", " blubb a"), null)
        );
    }
}