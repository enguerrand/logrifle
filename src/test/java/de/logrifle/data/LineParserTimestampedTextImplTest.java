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

package de.logrifle.data;

import de.logrifle.data.parsing.Line;
import de.logrifle.data.parsing.LineParseResult;
import de.logrifle.data.parsing.LineParserTimestampedTextImpl;
import de.logrifle.data.parsing.TestLinesFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LineParserTimestampedTextImplTest {

    @Test
    void parse() {
        LineParserTimestampedTextImpl parser = new LineParserTimestampedTextImpl();
        String raw = "DEBUG 23:12:33.234 - whatever log message";
        LineParseResult result = parser.parse(0, raw, TestLinesFactory.TEST_SOURCE);
        Line line = result.getParsedLine();
        long timestamp = line.getTimestamp();
        Assertions.assertTrue(result.isNewLine(), "newLine should be true");
        Assertions.assertEquals(raw, line.getRaw(), "Wrong raw content");
        Assertions.assertEquals(83553234L, timestamp, "Wrong timestamp");
    }
}