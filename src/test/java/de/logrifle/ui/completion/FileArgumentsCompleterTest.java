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
import org.junit.jupiter.api.Test;
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
import java.util.function.Function;
import java.util.stream.Stream;

class FileArgumentsCompleterTest {
    @TempDir
    Path workingDirectory;

    private static String SEP = System.getProperty("file.separator");

    Function<String, String> pathPlaceHolderExpander;
    private String homeDir;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @BeforeEach
    void setUp() throws IOException {
        homeDir = workingDirectory.toAbsolutePath().toString();
        pathPlaceHolderExpander = i -> i.replaceAll("~", homeDir);

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
    }

    @ParameterizedTest
    @MethodSource("getCompletionTestArguments")
    void testCompletion(String currentInput, List<String> expectedCompletions, List<String> expectedOptions) {
        FileArgumentsCompleter fileArgumentsCompleter = new FileArgumentsCompleter(workingDirectory, pathPlaceHolderExpander);
        CompletionResult completionResult = fileArgumentsCompleter.getCompletions(currentInput);
        Assertions.assertEquals(expectedCompletions, completionResult.getMatchingFullCompletions());
        Assertions.assertEquals(expectedOptions, completionResult.getOptions());
    }

    private static Stream<Arguments> getCompletionTestArguments() {
        return Stream.of(
                Arguments.of("foo"+SEP+"z", Collections.emptyList(), Collections.emptyList()),
                Arguments.of("", Collections.singletonList("foo"+SEP), Collections.singletonList("foo")),
                Arguments.of("f", Collections.singletonList("foo"+SEP), Collections.singletonList("foo")),
                Arguments.of("foo", Collections.singletonList("foo"+SEP), Collections.singletonList("foo")),
                Arguments.of("foo"+SEP, Arrays.asList("foo"+SEP+"bar"+SEP, "foo/bas"+SEP), Arrays.asList("bar", "bas")),
                Arguments.of("foo"+SEP+"b", Arrays.asList("foo"+SEP+"bar"+SEP, "foo"+SEP+"bas"+SEP), Arrays.asList("bar", "bas")),
                Arguments.of("foo"+SEP+"bar", Collections.singletonList("foo"+SEP+"bar"+SEP), Collections.singletonList("bar")),
                Arguments.of("foo"+SEP+"bar"+SEP, Collections.singletonList("foo"+SEP+"bar"+SEP+"bas"), Collections.singletonList("bas")),
                Arguments.of("foo"+SEP+"bar"+SEP+"ba", Collections.singletonList("foo"+SEP+"bar"+SEP+"bas"), Collections.singletonList("bas")),
                Arguments.of("foo"+SEP+"bar"+SEP+"bas", Collections.singletonList("foo"+SEP+"bar"+SEP+"bas"), Collections.singletonList("bas")),
                Arguments.of("foo"+SEP+"bar"+SEP+"baz", Collections.emptyList(), Collections.emptyList()),
                Arguments.of("foo"+SEP+"bas", Collections.singletonList("foo"+SEP+"bas"+SEP), Collections.singletonList("bas")),
                Arguments.of(
                        "foo"+SEP+"bas"+SEP,
                        Arrays.asList("foo"+SEP+"bas"+SEP+"bar", "foo"+SEP+"bas"+SEP+"barDir"+SEP+"", "foo"+SEP+"bas"+SEP+"baz"),
                        Arrays.asList("bar", "barDir", "baz")
                ),
                Arguments.of(
                        "foo"+SEP+"bas"+SEP+"barDir"+SEP,
                        Arrays.asList("foo"+SEP+"bas"+SEP+"barDir"+SEP+"subBar", "foo"+SEP+"bas"+SEP+"barDir"+SEP+"subBaz"),
                        Arrays.asList("subBar", "subBaz")
                )
        );
    }

    @Test
    void testPlaceHolderCompletion() {
        FileArgumentsCompleter fileArgumentsCompleter = new FileArgumentsCompleter(workingDirectory, pathPlaceHolderExpander);
        CompletionResult completionResult = fileArgumentsCompleter.getCompletions("~/f");
        List<String> expectedCompletions = Collections.singletonList(homeDir + "/foo/");
        List<String> expectedOptions = Collections.singletonList("foo");
        Assertions.assertEquals(expectedCompletions, completionResult.getMatchingFullCompletions());
        Assertions.assertEquals(expectedOptions, completionResult.getOptions());
    }

    @ParameterizedTest
    @MethodSource("getCompletionParentTestArguments")
    void testCompletionParent(String currentInput, List<String> expectedCompletions, List<String> expectedOptions) {
        FileArgumentsCompleter fileArgumentsCompleter = new FileArgumentsCompleter(workingDirectory.resolve("foo"), pathPlaceHolderExpander);
        CompletionResult fullCompletions = fileArgumentsCompleter.getCompletions(currentInput);
        Assertions.assertEquals(expectedCompletions, fullCompletions.getMatchingFullCompletions());
        Assertions.assertEquals(expectedOptions, fullCompletions.getOptions());
    }

    private static Stream<Arguments> getCompletionParentTestArguments() {
        return Stream.of(
                Arguments.of("."+SEP+"z", Collections.emptyList(), Collections.emptyList()),
                Arguments.of("", Arrays.asList("bar"+SEP, "bas"+SEP), Arrays.asList("bar", "bas")),
                Arguments.of("f", Collections.emptyList(), Collections.emptyList()),
                Arguments.of(".", Collections.emptyList(), Collections.emptyList()),
                Arguments.of("."+SEP, Arrays.asList("."+SEP+"bar"+SEP, "."+SEP+"bas"+SEP), Arrays.asList("bar", "bas")),
                Arguments.of("."+SEP+"b", Arrays.asList("."+SEP+"bar/", "."+SEP+"bas"+SEP), Arrays.asList("bar", "bas")),
                Arguments.of("."+SEP+"bar", Collections.singletonList("."+SEP+"bar"+SEP), Collections.singletonList("bar")),
                Arguments.of("."+SEP+"bar"+SEP, Collections.singletonList("."+SEP+"bar"+SEP+"bas"), Collections.singletonList("bas")),
                Arguments.of("."+SEP+"bar"+SEP+"ba", Collections.singletonList("."+SEP+"bar"+SEP+"bas"), Collections.singletonList("bas")),
                Arguments.of("."+SEP+"bar"+SEP+"bas", Collections.singletonList("."+SEP+"bar"+SEP+"bas"), Collections.singletonList("bas")),
                Arguments.of("."+SEP+"bar"+SEP+"baz", Collections.emptyList(), Collections.emptyList()),
                Arguments.of("."+SEP+"bas", Collections.singletonList("."+SEP+"bas"+SEP), Collections.singletonList("bas")),
                Arguments.of(
                        "."+SEP+"bas"+SEP,
                        Arrays.asList("."+SEP+"bas"+SEP+"bar", "."+SEP+"bas"+SEP+"barDir"+SEP, "."+SEP+"bas"+SEP+"baz"),
                        Arrays.asList("bar", "barDir", "baz")
                ),
                Arguments.of(
                        "."+SEP+"bas"+SEP+"barDir"+SEP,
                        Arrays.asList("."+SEP+"bas"+SEP+"barDir"+SEP+"subBar", "."+SEP+"bas"+SEP+"barDir"+SEP+"subBaz"),
                        Arrays.asList("subBar", "subBaz")
                ),
                Arguments.of(".."+SEP+"foo"+SEP+"z", Collections.emptyList(), Collections.emptyList()),
                Arguments.of(".."+SEP, Collections.singletonList(".."+SEP+"foo"+SEP), Collections.singletonList("foo")),
                Arguments.of(".."+SEP+"f", Collections.singletonList(".."+SEP+"foo"+SEP), Collections.singletonList("foo")),
                Arguments.of(".."+SEP+"foo", Collections.singletonList(".."+SEP+"foo"+SEP), Collections.singletonList("foo")),
                Arguments.of(".."+SEP+"foo"+SEP, Arrays.asList(".."+SEP+"foo"+SEP+"bar"+SEP, ".."+SEP+"foo"+SEP+"bas"+SEP), Arrays.asList("bar", "bas")),
                Arguments.of(".."+SEP+"foo"+SEP+"b", Arrays.asList(".."+SEP+"foo"+SEP+"bar"+SEP, ".."+SEP+"foo"+SEP+"bas"+SEP), Arrays.asList("bar", "bas")),
                Arguments.of(".."+SEP+"foo"+SEP+"bar", Collections.singletonList(".."+SEP+"foo"+SEP+"bar"+SEP), Collections.singletonList("bar")),
                Arguments.of(".."+SEP+"foo"+SEP+"bar"+SEP, Collections.singletonList(".."+SEP+"foo"+SEP+"bar"+SEP+"bas"), Collections.singletonList("bas")),
                Arguments.of(".."+SEP+"foo"+SEP+"bar"+SEP+"ba", Collections.singletonList(".."+SEP+"foo"+SEP+"bar"+SEP+"bas"), Collections.singletonList("bas")),
                Arguments.of(".."+SEP+"foo"+SEP+"bar"+SEP+"bas", Collections.singletonList(".."+SEP+"foo"+SEP+"bar"+SEP+"bas"), Collections.singletonList("bas")),
                Arguments.of(".."+SEP+"foo"+SEP+"bar"+SEP+"baz", Collections.emptyList(), Collections.emptyList()),
                Arguments.of(".."+SEP+"foo"+SEP+"bas", Collections.singletonList(".."+SEP+"foo"+SEP+"bas"+SEP), Collections.singletonList("bas")),
                Arguments.of(
                        ".."+SEP+"foo"+SEP+"bas"+SEP,
                        Arrays.asList(".."+SEP+"foo"+SEP+"bas"+SEP+"bar", ".."+SEP+"foo"+SEP+"bas"+SEP+"barDir"+SEP, ".."+SEP+"foo"+SEP+"bas"+SEP+"baz"),
                        Arrays.asList("bar", "barDir", "baz")
                ),
                Arguments.of(
                        ".."+SEP+"foo"+SEP+"bas"+SEP+"barDir"+SEP,
                        Arrays.asList(".."+SEP+"foo"+SEP+"bas"+SEP+"barDir"+SEP+"subBar", ".."+SEP+"foo"+SEP+"bas"+SEP+"barDir"+SEP+"subBaz"),
                        Arrays.asList("subBar", "subBaz")
                )
        );
    }
}