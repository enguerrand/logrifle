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

package de.logrifle.data.parsing;

import com.googlecode.lanterna.TextColor;

import java.util.ArrayList;
import java.util.List;

public class TestLinesFactory {
    private static final LineParser PARSER = new LineParserTimestampedTextImpl();

    public static List<Line> buildTestLines() {
        List<Line> lines = new ArrayList<>();
        lines.add(PARSER.parse(0, "23:09:37.129 line content 0", "label", TextColor.ANSI.DEFAULT).getParsedLine());
        lines.add(PARSER.parse(1, "23:09:38.131 line content 1", "label", TextColor.ANSI.DEFAULT).getParsedLine());
        lines.add(PARSER.parse(2, "23:09:38.151 line content 2", "label", TextColor.ANSI.DEFAULT).getParsedLine());
        lines.add(PARSER.parse(3, "23:09:38.519 line content 3", "label", TextColor.ANSI.DEFAULT).getParsedLine());
        lines.add(PARSER.parse(4, "23:09:39.100 line content 4", "label", TextColor.ANSI.DEFAULT).getParsedLine());
        lines.add(PARSER.parse(5, "23:09:41.164 line content 5", "label", TextColor.ANSI.DEFAULT).getParsedLine());
        lines.add(PARSER.parse(6, "23:09:43.642 line content 6", "label", TextColor.ANSI.DEFAULT).getParsedLine());
        lines.add(PARSER.parse(7, "23:10:12.129 line content 7", "label", TextColor.ANSI.DEFAULT).getParsedLine());
        lines.add(PARSER.parse(8, "23:59:38.001 line content 8", "label", TextColor.ANSI.DEFAULT).getParsedLine());
        return lines;
    }
}
