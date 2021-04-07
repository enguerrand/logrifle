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

import de.logrifle.data.views.LineSourceTestImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

class LineTest {
    public static final String LINE_SOURCE = "foobarSource";
    private LineParserTextImpl lineParserText;
    private LineSourceTestImpl lineSourceTest;
    public static final String RAW_LINE_CONTENT = "foo bar baz whatever";

    @BeforeEach
    void setUp() {
        lineParserText = new LineParserTextImpl();
        lineSourceTest = new LineSourceTestImpl(LINE_SOURCE);
    }

    @ParameterizedTest
    @MethodSource("exportArgs")
    void export(int labelLength, String expectedExport) {
        Line line = lineParserText.parse(42, RAW_LINE_CONTENT, lineSourceTest).getParsedLine();
        Collection<String> exported = line.export(labelLength);
        Assertions.assertEquals(1, exported.size());
        String first = exported.iterator().next();
        Assertions.assertEquals(expectedExport, first);
    }

    private static Stream<Arguments> exportArgs() {
        return Stream.of(
                Arguments.of(0, RAW_LINE_CONTENT),
                Arguments.of(1, "f" + Line.EXPORT_LABEL_SEPARATOR + RAW_LINE_CONTENT),
                Arguments.of(11, "foobarSourc" + Line.EXPORT_LABEL_SEPARATOR + RAW_LINE_CONTENT),
                Arguments.of(12, "foobarSource" + Line.EXPORT_LABEL_SEPARATOR + RAW_LINE_CONTENT),
                Arguments.of(13, "foobarSource " + Line.EXPORT_LABEL_SEPARATOR + RAW_LINE_CONTENT),
                Arguments.of(14, "foobarSource  " + Line.EXPORT_LABEL_SEPARATOR + RAW_LINE_CONTENT)
        );
    }

    @Test
    void exportWithAdditionalLines() {
        Line line = lineParserText.parse(42, RAW_LINE_CONTENT, lineSourceTest).getParsedLine();
        line.appendAdditionalLine("secondLine");
        line.appendAdditionalLine("thirdLine");
        List<String> export = new ArrayList<>(line.export(1));
        List<String> expected = Arrays.asList(
                "f" + Line.EXPORT_LABEL_SEPARATOR + RAW_LINE_CONTENT,
                "f" + Line.EXPORT_LABEL_SEPARATOR + "secondLine",
                "f" + Line.EXPORT_LABEL_SEPARATOR + "thirdLine"
        );
        Assertions.assertEquals(expected, export);
    }
}