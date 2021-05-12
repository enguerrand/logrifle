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

package de.logrifle.ui.completion;

import de.logrifle.ui.MainController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CommandAutoCompleterTest {
    private CommandAutoCompleter commandAutoCompleter;

    @BeforeEach
    void setUp() {
        AbstractArgumentCompleter foobarArgCompleter = new AbstractArgumentCompleter("foobar") {
            final CommandAutoCompleter firstArgCompleter = new CommandAutoCompleter(
                    "",
                    Arrays.asList(
                            "full/foobarFirstArg1a",
                            "full/foobarFirstArg2b"
                    )
            );
            @Override
            public CompletionResult getCompletions(String currentArgs) {
                CompletionResult intermediate = firstArgCompleter.getCompletion(currentArgs);
                return new CompletionResult(
                        intermediate.getOptions().stream()
                                .map(c -> c.substring(5))
                                .collect(Collectors.toList()),
                        intermediate.getOptions()
                );
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
    void getMatching(String currentInput, List<String> expectedFullCompletions, List<String> expectedOptions, int expectedMaximumCommandLength) {
        CompletionResult completionResult = commandAutoCompleter.getCompletion(currentInput);
        List<String> fullCompletions = completionResult.getMatchingFullCompletions();
        List<String> options = completionResult.getOptions();
        Assertions.assertEquals(expectedFullCompletions, fullCompletions);
        Assertions.assertEquals(expectedOptions, options);
        Assertions.assertEquals(expectedMaximumCommandLength, commandAutoCompleter.getMaximumCommandLength(currentInput));
    }

    private static Stream<Arguments> getMatchingArgs() {
        return Stream.of(
                Arguments.of(":", Arrays.asList("foo", "foobar", "foobas", "bar"), Arrays.asList("foo", "foobar", "foobas", "bar"), 7),
                Arguments.of(": ", Collections.emptyList(), Collections.emptyList(), 2),
                Arguments.of(":f", Arrays.asList("foo", "foobar", "foobas"), Arrays.asList("foo", "foobar", "foobas"), 7),
                Arguments.of(":foo", Arrays.asList("foo", "foobar", "foobas"), Arrays.asList("foo", "foobar", "foobas"), 7),
                Arguments.of(":foob", Arrays.asList("foobar", "foobas"), Arrays.asList("foobar", "foobas"), 7),
                Arguments.of(":fooba", Arrays.asList("foobar", "foobas"), Arrays.asList("foobar", "foobas"), 7),
                Arguments.of(":foobar", Collections.singletonList("foobar"), Collections.singletonList("foobar"), 7),
                Arguments.of(":bar", Collections.singletonList("bar"), Collections.singletonList("bar"), 4),
                Arguments.of(":bar arg", Collections.emptyList(), Collections.emptyList(), 8),
                Arguments.of(":zzz", Collections.emptyList(), Collections.emptyList(), 4),
                Arguments.of(":", Arrays.asList("foo", "foobar", "foobas", "bar"), Arrays.asList("foo", "foobar", "foobas", "bar"), 7),
                Arguments.of(":foobar ", Arrays.asList("foobar full/foobarFirstArg1a", "foobar full/foobarFirstArg2b"), Arrays.asList("foobarFirstArg1a", "foobarFirstArg2b"), 29),
                Arguments.of(":foobar full/foobarFirstArg", Arrays.asList("foobar full/foobarFirstArg1a", "foobar full/foobarFirstArg2b"), Arrays.asList("foobarFirstArg1a", "foobarFirstArg2b"), 29),
                Arguments.of(":foobar full/foobarFirstArg1", Collections.singletonList("foobar full/foobarFirstArg1a"), Collections.singletonList("foobarFirstArg1a"), 29),
                Arguments.of(":foobar foobas", Collections.emptyList(), Collections.emptyList(), 14)
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
                Arguments.of(": ", ": "),
                Arguments.of(":", ":"),
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
                Arguments.of(":foobar full/foobarFirst", ":foobar full/foobarFirstArg"),
                Arguments.of(":foobar full/foobarFirstArg1", ":foobar full/foobarFirstArg1a")
        );
    }

    @Test
    void testExpandedCompletions() {
        commandAutoCompleter = new CommandAutoCompleter(
                MainController.COMMAND_PREFIX,
                Collections.singletonList("foo"),
                new AbstractArgumentCompleter("foo") {
                    @Override
                    public CompletionResult getCompletions(String currentInput) {
                        return new CompletionResult(Collections.singletonList("sub"), Collections.singletonList("/home/user/sub"));
                    }
                }
        );

        String currentInput = ":foo ~/s";
        String expectedFullCompletion = "foo /home/user/sub";

        CompletionResult completionResult = commandAutoCompleter.getCompletion(currentInput);
        Assertions.assertEquals(Collections.singletonList(expectedFullCompletion), completionResult.getMatchingFullCompletions());
        Assertions.assertEquals(Collections.singletonList("sub"), completionResult.getOptions());
        String completion = commandAutoCompleter.complete(currentInput);
        Assertions.assertEquals(":" + expectedFullCompletion, completion);
    }
}