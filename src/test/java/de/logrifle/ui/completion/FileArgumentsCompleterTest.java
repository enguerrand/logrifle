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
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

class FileArgumentsCompleterTest {
    @TempDir
    Path workingDirectory;


    @SuppressWarnings("ResultOfMethodCallIgnored")
    @BeforeEach
    void setUp() throws IOException {
        for (Path sub : Arrays.asList(
                Paths.get("foo", "bar", "bas"),
                Paths.get("foo", "bas", "baz"),
                Paths.get("foo", "bas", "bar"),
                Paths.get("foo", "bas", "barDir", "subBar"),
                Paths.get("foo", "bas", "barDir", "subBaz")
        )) {
            File file = workingDirectory.resolve(sub).toFile();
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        System.out.println(workingDirectory.toAbsolutePath().toString());
    }

    @ParameterizedTest
    @MethodSource("getCompletionTestArguments")
    void testCompletion(String currentInput, List<String> expectedCompletions) {
        FileArgumentsCompleter fileArgumentsCompleter = new FileArgumentsCompleter(workingDirectory);
        List<String> completions = fileArgumentsCompleter.getCompletions(currentInput);
        Assertions.assertEquals(expectedCompletions, completions);
    }

    private static Stream<Arguments> getCompletionTestArguments() {
        return Stream.of(
                Arguments.of("foo/z", Collections.emptyList()),
                Arguments.of("", Collections.singletonList("foo/")),
                Arguments.of("f", Collections.singletonList("foo/")),
                Arguments.of("foo", Collections.singletonList("foo/")),
                Arguments.of("foo/", Arrays.asList("foo/bar/", "foo/bas/")),
                Arguments.of("foo/b", Arrays.asList("foo/bar/", "foo/bas/")),
                Arguments.of("foo/bar", Collections.singletonList("foo/bar/")),
                Arguments.of("foo/bar/", Collections.singletonList("foo/bar/bas")),
                Arguments.of("foo/bar/ba", Collections.singletonList("foo/bar/bas")),
                Arguments.of("foo/bar/bas", Collections.singletonList("foo/bar/bas")),
                Arguments.of("foo/bar/baz", Collections.emptyList()),
                Arguments.of("foo/bas", Collections.singletonList("foo/bas/")),
                Arguments.of("foo/bas/", Arrays.asList("foo/bas/bar", "foo/bas/barDir/", "foo/bas/baz")),
                Arguments.of("foo/bas/barDir/", Arrays.asList("foo/bas/barDir/subBar", "foo/bas/barDir/subBaz"))
        );
    }

    @ParameterizedTest
    @MethodSource("getCompletionParentTestArguments")
    void testCompletionParent(String currentInput, List<String> expectedCompletions) {
        FileArgumentsCompleter fileArgumentsCompleter = new FileArgumentsCompleter(workingDirectory.resolve("foo"));
        List<String> completions = fileArgumentsCompleter.getCompletions(currentInput);
        Assertions.assertEquals(expectedCompletions, completions);
    }


    private static Stream<Arguments> getCompletionParentTestArguments() {
        return Stream.of(
                Arguments.of("./z", Collections.emptyList()),
                Arguments.of("", Arrays.asList("bar/", "bas/")),
                Arguments.of("f", Collections.emptyList()),
                Arguments.of(".", Collections.emptyList()),
                Arguments.of("./", Arrays.asList("./bar/", "./bas/")),
                Arguments.of("./b", Arrays.asList("./bar/", "./bas/")),
                Arguments.of("./bar", Collections.singletonList("./bar/")),
                Arguments.of("./bar/", Collections.singletonList("./bar/bas")),
                Arguments.of("./bar/ba", Collections.singletonList("./bar/bas")),
                Arguments.of("./bar/bas", Collections.singletonList("./bar/bas")),
                Arguments.of("./bar/baz", Collections.emptyList()),
                Arguments.of("./bas", Collections.singletonList("./bas/")),
                Arguments.of("./bas/", Arrays.asList("./bas/bar", "./bas/barDir/", "./bas/baz")),
                Arguments.of("./bas/barDir/", Arrays.asList("./bas/barDir/subBar", "./bas/barDir/subBaz")),
                Arguments.of("../foo/z", Collections.emptyList()),
                Arguments.of("../", Collections.singletonList("../foo/")),
                Arguments.of("../f", Collections.singletonList("../foo/")),
                Arguments.of("../foo", Collections.singletonList("../foo/")),
                Arguments.of("../foo/", Arrays.asList("../foo/bar/", "../foo/bas/")),
                Arguments.of("../foo/b", Arrays.asList("../foo/bar/", "../foo/bas/")),
                Arguments.of("../foo/bar", Collections.singletonList("../foo/bar/")),
                Arguments.of("../foo/bar/", Collections.singletonList("../foo/bar/bas")),
                Arguments.of("../foo/bar/ba", Collections.singletonList("../foo/bar/bas")),
                Arguments.of("../foo/bar/bas", Collections.singletonList("../foo/bar/bas")),
                Arguments.of("../foo/bar/baz", Collections.emptyList()),
                Arguments.of("../foo/bas", Collections.singletonList("../foo/bas/")),
                Arguments.of("../foo/bas/", Arrays.asList("../foo/bas/bar", "../foo/bas/barDir/", "../foo/bas/baz")),
                Arguments.of("../foo/bas/barDir/", Arrays.asList("../foo/bas/barDir/subBar", "../foo/bas/barDir/subBaz"))
        );
    }
}