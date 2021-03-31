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

package de.logrifle.ui.completion;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class IndexArgumentsCompleterTest {

    private IndexArgumentsCompleter completer;
    private AtomicInteger size;

    @BeforeEach
    void setUp() {
        size = new AtomicInteger(0);
        completer = new IndexArgumentsCompleter(size::get, "foo", "bar");
    }

    @ParameterizedTest
    @MethodSource("getCompletionTestArgs")
    void testCompletion(String currentInput, int sizeValue, String expectedCompletionsCsv) {
        size.set(sizeValue);
        List<String> expected = Arrays.stream(expectedCompletionsCsv.split(";")).collect(Collectors.toList());
        CompletionResult completionResult = completer.getCompletions(currentInput);
        List<String> completions = completionResult.getMatchingFullCompletions();
        Assertions.assertEquals(completions, completionResult.getOptions());
        if (expectedCompletionsCsv.isEmpty()) {
            Assertions.assertTrue(completions.isEmpty());
        } else {
            Assertions.assertEquals(expected, completions);
        }
    }

    private static Stream<Arguments> getCompletionTestArgs() {
        return Stream.of(
                Arguments.of("", 0, ""),
                Arguments.of("", 1, "0"),
                Arguments.of("", 5, "0;1;2;3;4"),
                Arguments.of("0", 1, "0"),
                Arguments.of("1", 1, ""),
                Arguments.of("2", 5, "2"),
                Arguments.of("2", 20, "2"),
                Arguments.of("2", 21, "2;20")
        );
    }
}
