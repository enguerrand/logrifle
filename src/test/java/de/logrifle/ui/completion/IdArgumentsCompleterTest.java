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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class IdArgumentsCompleterTest {

    private List<Integer> ids;
    private IdArgumentsCompleter completer;

    @BeforeEach
    void setUp() {
        ids = new ArrayList<>();
        Supplier<Collection<Integer>> idsSupplier;
        idsSupplier = () -> ids;
        completer = new IdArgumentsCompleter(idsSupplier, "foo", "bar");
    }

    @ParameterizedTest
    @MethodSource("getCompletionTestArgs")
    void testCompletion(String currentInput, String idOptionsCsv, String expectedCompletionsCsv) {
        ids.addAll(Arrays.stream(idOptionsCsv.split(";")).map(Integer::parseInt).collect(Collectors.toList()));
        List<String> expected = Arrays.stream(expectedCompletionsCsv.split(";")).collect(Collectors.toList());
        CompletionResult completionsResult = completer.getCompletions(currentInput);
        List<String> completions = completionsResult.getMatchingFullCompletions();
        Assertions.assertEquals(completions, completionsResult.getOptions());
        if (expectedCompletionsCsv.isEmpty()) {
            Assertions.assertTrue(completions.isEmpty());
        } else {
            Assertions.assertEquals(expected, completions);
        }
    }

    private static Stream<Arguments> getCompletionTestArgs() {
        return Stream.of(
                Arguments.of("", "1;2;10;100;221", "1;2;10;100;221"),
                Arguments.of("1", "1;2;10;100;221", "1;10;100"),
                Arguments.of("10", "1;2;10;100;221", "10;100"),
                Arguments.of("100", "1;2;10;100;221", "100"),
                Arguments.of("101", "1;2;10;100;221", "")
        );
    }
}
