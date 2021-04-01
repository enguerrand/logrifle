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
import de.logrifle.ui.LineLabelDisplayMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class LinesTest {
    public static final String LINE_SOURCE_1 = "foobarSource1";
    public static final String LINE_SOURCE_2 = "bazSource2";
    private LineParserTextImpl lineParserText;
    private LineSourceTestImpl lineSourceTest1;
    private LineSourceTestImpl lineSourceTest2;
    public static final String RAW_LINE_CONTENT_1 = "foo bar baz whatever";
    public static final String RAW_LINE_CONTENT_2 = "foo bar baz something else";

    @BeforeEach
    void setUp() {
        lineParserText = new LineParserTextImpl();
        lineSourceTest1 = new LineSourceTestImpl(LINE_SOURCE_1);
        lineSourceTest2 = new LineSourceTestImpl(LINE_SOURCE_2);
    }

    @Test
    void exportDisplayModeLong() {
        Line line1 = lineParserText.parse(42, RAW_LINE_CONTENT_1, lineSourceTest1).getParsedLine();
        line1.appendAdditionalLine("secondLine");
        line1.appendAdditionalLine("thirdLine");
        Line line2 = lineParserText.parse(47, RAW_LINE_CONTENT_2, lineSourceTest2).getParsedLine();
        List<String> export = new ArrayList<>(Lines.export(Arrays.asList(line1, line2), LineLabelDisplayMode.LONG));
        List<String> expected = Arrays.asList(
                LINE_SOURCE_1 + Line.EXPORT_LABEL_SEPARATOR + RAW_LINE_CONTENT_1,
                LINE_SOURCE_1 + Line.EXPORT_LABEL_SEPARATOR + "secondLine",
                LINE_SOURCE_1 + Line.EXPORT_LABEL_SEPARATOR + "thirdLine",
                LINE_SOURCE_2 + "   " + Line.EXPORT_LABEL_SEPARATOR + RAW_LINE_CONTENT_2
        );
        Assertions.assertEquals(expected, export);
    }

    @Test
    void exportDisplayModeShort() {
        Line line1 = lineParserText.parse(42, RAW_LINE_CONTENT_1, lineSourceTest1).getParsedLine();
        line1.appendAdditionalLine("secondLine");
        line1.appendAdditionalLine("thirdLine");
        Line line2 = lineParserText.parse(47, RAW_LINE_CONTENT_2, lineSourceTest2).getParsedLine();
        List<String> export = new ArrayList<>(Lines.export(Arrays.asList(line1, line2), LineLabelDisplayMode.SHORT));
        List<String> expected = Arrays.asList(
                "f" + Line.EXPORT_LABEL_SEPARATOR + RAW_LINE_CONTENT_1,
                "f" + Line.EXPORT_LABEL_SEPARATOR + "secondLine",
                "f" + Line.EXPORT_LABEL_SEPARATOR + "thirdLine",
                "b" + Line.EXPORT_LABEL_SEPARATOR + RAW_LINE_CONTENT_2
        );
        Assertions.assertEquals(expected, export);
    }

    @Test
    void exportDisplayModeNone() {
        Line line1 = lineParserText.parse(42, RAW_LINE_CONTENT_1, lineSourceTest1).getParsedLine();
        line1.appendAdditionalLine("secondLine");
        line1.appendAdditionalLine("thirdLine");
        Line line2 = lineParserText.parse(47, RAW_LINE_CONTENT_2, lineSourceTest2).getParsedLine();
        List<String> export = new ArrayList<>(Lines.export(Arrays.asList(line1, line2), LineLabelDisplayMode.NONE));
        List<String> expected = Arrays.asList(
                RAW_LINE_CONTENT_1,
                "secondLine",
                "thirdLine",
                RAW_LINE_CONTENT_2
        );
        Assertions.assertEquals(expected, export);
    }
}