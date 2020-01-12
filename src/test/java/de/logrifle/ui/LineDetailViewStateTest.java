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

import com.googlecode.lanterna.TextColor;
import de.logrifle.data.parsing.Line;
import de.logrifle.data.parsing.LineParser;
import de.logrifle.data.parsing.LineParserTextImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class LineDetailViewStateTest {
    private final static LineParser P = new LineParserTextImpl();

    private static List<TopLinesToSkipTestCase> getTopLinesToSkipTestCases() {
        return Arrays.asList(
                new TopLinesToSkipTestCase(3, 4, 3, 10),
                new TopLinesToSkipTestCase(0, 10, 0, 10),
                new TopLinesToSkipTestCase(0, 10, 1, 8),
                new TopLinesToSkipTestCase(1, 10, 2, 8),
                new TopLinesToSkipTestCase(2, 10, 3, 8),
                new TopLinesToSkipTestCase(3, 10, 3, 9),
                new TopLinesToSkipTestCase(3, 10, 3, 10)
        );
    }

    @Test
    void getTopLinesToSkipNoDetail() {
        LineDetailViewState lineDetailViewState = new LineDetailViewState();
        Assertions.assertEquals(0, lineDetailViewState.getTopLinesCountToSkip(Arrays.asList(
                P.parse(0, "first", "FOO", TextColor.ANSI.DEFAULT).getParsedLine(),
                P.parse(1, "second", "FOO", TextColor.ANSI.DEFAULT).getParsedLine()
        )));
    }

    @ParameterizedTest
    @MethodSource("getTopLinesToSkipTestCases")
    void getTopLinesToSkip(TopLinesToSkipTestCase testCase) {
        LineDetailViewState lineDetailViewState = new LineDetailViewState();
        Line detailedLine = P.parse(testCase.getDetailedLineIndex(), "detail", "FOO", TextColor.ANSI.DEFAULT).getParsedLine();
        for (int i = 0; i < testCase.getAdditionalLineCount(); i++) {
            detailedLine.appendAdditionalLine("foo"+i);
        }
        lineDetailViewState.set(detailedLine);

        List<Line> lines = new ArrayList<>();
        int linesCount = testCase.getLinesCount();
        for (int i = 0; i < linesCount; i++) {
            if (i == detailedLine.getIndex()) {
                lines.add(detailedLine);
            } else {
                lines.add(P.parse(i, "other" + i, "FOO", TextColor.ANSI.DEFAULT).getParsedLine());
            }
        }
        int actual = lineDetailViewState.getTopLinesCountToSkip(lines);
        Assertions.assertEquals(testCase.getExpectedResult(), actual);
    }

    private static class TopLinesToSkipTestCase {
        private final int expectedResult;
        private final int linesCount;
        private final int detailedLineIndex;
        private final int additionalLineCount;

        private TopLinesToSkipTestCase(int expectedResult, int linesCount, int detailedLineIndex, int additionalLineCount) {
            this.expectedResult = expectedResult;
            this.linesCount = linesCount;
            this.detailedLineIndex = detailedLineIndex;
            this.additionalLineCount = additionalLineCount;
        }

        private int getExpectedResult() {
            return expectedResult;
        }

        private int getLinesCount() {
            return linesCount;
        }

        public int getDetailedLineIndex() {
            return detailedLineIndex;
        }

        private int getAdditionalLineCount() {
            return additionalLineCount;
        }
    }
}