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

package de.logrifle.base;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class StringsTest {

    @ParameterizedTest
    @CsvSource({
            "foobar____,foobar,10,_,false",
            "____foobar,foobar,10,_,true",
            "!!foobar,foobar,8,!,true",
            "foobar,foobar,4,!,true",

    })
    void pad(String expectedResult, String inputString, String desiredLength, String paddingContent, String beginning) {
        String padded = Strings.pad(inputString, Integer.parseInt(desiredLength), paddingContent, Boolean.parseBoolean(beginning));
        Assertions.assertEquals(expectedResult, padded);
    }

    @ParameterizedTest
    @CsvSource({
            "abcdef,7,abcdef",
            "abcdef,6,abcdef",
            "abcdef,5,ab...",
            "abcdef,4,a...",
            "abcdef,3,...",
            "abcdef,2,..",
            "abcdef,1,.",
            "abcdef,0,",

    })
    void truncate(String input, String maxLength, String expectedOutput) {
        if (expectedOutput == null) {
            expectedOutput = "";
        }
        Assertions.assertEquals(expectedOutput, Strings.truncateString(input, Integer.parseInt(maxLength)));
    }

    @ParameterizedTest
    @CsvSource({
            "abcdef,abcdef",
            " abcdef,abcdef",
            "  abcdef,abcdef",
            "   abcdef,abcdef",
            "       abcdef,abcdef",
            "   a    abcdef,a    abcdef",
            "   abcdef  ,abcdef  ",

    })
    void trimStart(String input, String expectedOutput) {
        if (expectedOutput == null) {
            expectedOutput = "";
        }
        Assertions.assertEquals(expectedOutput, Strings.trimStart(input));
    }

    @ParameterizedTest
    @CsvSource({
            "abcdef,2,ab,cdef",
            "abcdef,0,,abcdef",
            "abcdef,6,abcdef,"
    })
    void tokenizeAt(String input, String indexAsString, String expectedFirst, String expectedSecond) {
        String[] tokenized = Strings.tokenizeAt(input, Integer.parseInt(indexAsString));
        Assertions.assertEquals(2, tokenized.length);
        Assertions.assertEquals(expectedFirst == null ? "" : expectedFirst, tokenized[0]);
        Assertions.assertEquals(expectedSecond == null ? "" : expectedSecond, tokenized[1]);
    }

    @ParameterizedTest
    @MethodSource("firstWordArguments")
    void findFirstWordStartOrEnd(String input, int expectedIndex) {
        int index = Strings.findFirstWordStartOrEnd(input);
        Assertions.assertEquals(expectedIndex, index);
    }

    private static Stream<Arguments> firstWordArguments() {
        return Stream.of(
                Arguments.of("abcdef",6),
                Arguments.of(" ",1),
                Arguments.of("abcdef ",7),
                Arguments.of(" abcdef",1),
                Arguments.of(" abc def",1),
                Arguments.of("abc def",4),
                Arguments.of("abc def ghi",4),
                Arguments.of("abc  def ghi",5),
                Arguments.of("abc     def ghi",8)
        );
    }

    @ParameterizedTest
    @MethodSource("lastWordArguments")
    void findLastWordStartOrStart(String input, int expectedIndex) {
        int index = Strings.findLastWordStartOrStart(input);
        Assertions.assertEquals(expectedIndex, index);
    }

    private static Stream<Arguments> lastWordArguments() {
        return Stream.of(
                Arguments.of("abcdef",0),
                Arguments.of(" abcdef",1),
                Arguments.of(" abcdef ",1),
                Arguments.of(" abcdef  ",1),
                Arguments.of("abc def",4),
                Arguments.of("abc   def",6),
                Arguments.of("abc def ghi",8),
                Arguments.of(" ",0)
        );
    }

    @ParameterizedTest
    @MethodSource("nextWordArguments")
    void findNextWordStartOrEnd(String input, int currentIndex, int expectedIndex) {
        int index = Strings.findNextWordStartOrEnd(input, currentIndex);
        Assertions.assertEquals(expectedIndex, index);
    }

    private static Stream<Arguments> nextWordArguments() {
        return Stream.of(
                Arguments.of("abcdef",0, 6),
                Arguments.of("abcdef",3, 6),
                Arguments.of("abcdef",5, 6),
                Arguments.of(" abcdef",0, 1),
                Arguments.of(" abcdef",1, 7),
                Arguments.of(" abcdef",2, 7),
                Arguments.of("abc def",0, 4),
                Arguments.of("abc def",3, 4),
                Arguments.of("abc def",4, 7),
                Arguments.of("abc def ghi",4, 8),
                Arguments.of("abc def ghi",6, 8),
                Arguments.of("abc def ghi",7, 8),
                Arguments.of("abc def  ghi",7, 9),
                Arguments.of("abc def  ghi",8, 9),
                Arguments.of("abc def  ghi",9, 12),
                Arguments.of("abc def ghi",8, 11)
        );
    }


    @ParameterizedTest
    @MethodSource("previousWordArguments")
    void findPreviousWordStartOrStart(String input, int currentIndex, int expectedIndex) {
        int index = Strings.findPreviousWordStartOrStrt(input, currentIndex);
        Assertions.assertEquals(expectedIndex, index);
    }

    private static Stream<Arguments> previousWordArguments() {
        return Stream.of(
                Arguments.of("abcdef",0, 0),
                Arguments.of("abcdef",3, 0),
                Arguments.of("abcdef",6, 0),
                Arguments.of(" abcdef",0, 0),
                Arguments.of(" abcdef",1, 0),
                Arguments.of(" abcdef",2, 1),
                Arguments.of("abc def",3, 0),
                Arguments.of("abc def",4, 0),
                Arguments.of("abc def",5, 4),
                Arguments.of("abc def",7, 4),
                Arguments.of("abc def ",8, 4),
                Arguments.of("abc def ghi",4, 0),
                Arguments.of("abc def ghi",7, 4),
                Arguments.of("abc def ghi",8, 4),
                Arguments.of("abc def  ghi",9, 4),
                Arguments.of("abc def  ghi",9, 4),
                Arguments.of("abc def  ghi",10, 9),
                Arguments.of("abc def  ghi ",12, 9),
                Arguments.of("abc def  ghi ",13, 9),
                Arguments.of("abc def ghi",11, 8)
        );
    }
}