package de.logrifle.data;

import com.googlecode.lanterna.TextColor;
import de.logrifle.data.parsing.Line;
import de.logrifle.data.parsing.LineParseResult;
import de.logrifle.data.parsing.LineParserTimestampedTextImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LineParserTimestampedTextImplTest {

    @Test
    void parse() {
        LineParserTimestampedTextImpl parser = new LineParserTimestampedTextImpl();
        String raw = "DEBUG 23:12:33.234 - whatever log message";
        LineParseResult result = parser.parse(0, raw, "dummy", TextColor.ANSI.DEFAULT);
        Line line = result.getParsedLine();
        long timestamp = line.getTimestamp();
        Assertions.assertTrue(result.isNewLine(), "newLine should be true");
        Assertions.assertEquals(raw, line.getRaw(), "Wrong raw content");
        Assertions.assertEquals(83553234L, timestamp, "Wrong timestamp");
    }
}