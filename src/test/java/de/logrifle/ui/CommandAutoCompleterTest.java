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

package de.logrifle.ui;

import de.logrifle.ui.completion.AbstractArgumentCompleter;
import de.logrifle.ui.completion.CommandAutoCompleter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

class CommandAutoCompleterTest {
    private CommandAutoCompleter commandAutoCompleter;

    @BeforeEach
    void setUp() {
        AbstractArgumentCompleter foobarArgCompleter = new AbstractArgumentCompleter("foobar") {
            final CommandAutoCompleter firstArgCompleter = new CommandAutoCompleter(
                    "",
                    Arrays.asList(
                            "foobarFirstArg1a",
                            "foobarFirstArg2b"
                    )
            );
            @Override
            public List<String> getCompletions(String currentArgs) {
                return firstArgCompleter.getMatching(currentArgs);
            }
        };
        commandAutoCompleter = new CommandAutoCompleter(
                MainController.COMMAND_PREFIX,
                Arrays.asList(
                        "foo",
                        "foobar",
                        "foobas",
                        "bar"
                ),
                foobarArgCompleter
        );
    }

    @ParameterizedTest
    @MethodSource("getMatchingArgs")
    void getMatching(String currentInput, List<String> expectedMatches) {
        List<String> matches = commandAutoCompleter.getMatching(currentInput);
        Assertions.assertEquals(expectedMatches, matches);
    }

    private static Stream<Arguments> getMatchingArgs() {
        return Stream.of(
                Arguments.of(":f", Arrays.asList("foo", "foobar", "foobas")),
                Arguments.of(":foo", Arrays.asList("foo", "foobar", "foobas")),
                Arguments.of(":foob", Arrays.asList("foobar", "foobas")),
                Arguments.of(":fooba", Arrays.asList("foobar", "foobas")),
                Arguments.of(":foobar", Collections.singletonList("foobar")),
                Arguments.of(":bar", Collections.singletonList("bar")),
                Arguments.of(":bar arg", Collections.emptyList()),
                Arguments.of(":zzz", Collections.emptyList()),
                Arguments.of(":", Arrays.asList("foo", "foobar", "foobas", "bar")),
                Arguments.of("", Collections.emptyList()),
                Arguments.of(":foobar ", Arrays.asList("foobarFirstArg1a", "foobarFirstArg2b")),
                Arguments.of(":foobar foobarFirstArg", Arrays.asList("foobarFirstArg1a", "foobarFirstArg2b")),
                Arguments.of(":foobar foobarFirstArg1", Collections.singletonList("foobarFirstArg1a")),
                Arguments.of(":foobar foobas", Collections.emptyList())
        );
    }

    @ParameterizedTest
    @MethodSource("getCompleteArguments")
    void complete(String currentInput, String expectedCompletion) {
        String completed = commandAutoCompleter.complete(currentInput);
        Assertions.assertEquals(expectedCompletion, completed, "Completion failed for current input "+currentInput);
    }

    private static Stream<Arguments> getCompleteArguments() {
        return Stream.of(
                Arguments.of(":f", ":foo"),
                Arguments.of(":fo", ":foo"),
                Arguments.of(":foo", ":foo"),
                Arguments.of(":foob", ":fooba"),
                Arguments.of(":fooba", ":fooba"),
                Arguments.of(":foobar", ":foobar"),
                Arguments.of(":foobar arg", ":foobar arg"),
                Arguments.of(":wups", ":wups"),
                Arguments.of(":b", ":bar"),
                Arguments.of(":ba", ":bar"),
                Arguments.of(":bar", ":bar"),
                Arguments.of(":zzz", ":zzz"),
                Arguments.of("zzz", "zzz"),
                Arguments.of(":foobar foobarFirst", ":foobar foobarFirstArg"),
                Arguments.of(":foobar foobarFirstArg1", ":foobar foobarFirstArg1a")
        );
    }

}