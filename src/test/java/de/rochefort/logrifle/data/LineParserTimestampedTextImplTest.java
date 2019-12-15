package de.rochefort.logrifle.data;

import de.rochefort.logrifle.data.parsing.Line;
import de.rochefort.logrifle.data.parsing.LineParseResult;
import de.rochefort.logrifle.data.parsing.LineParserTimestampedTextImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LineParserTimestampedTextImplTest {

    @Test
    void parse() {
        LineParserTimestampedTextImpl parser = new LineParserTimestampedTextImpl();
        String raw = "DEBUG 23:12:33.234 - whatever log message";
        LineParseResult result = parser.parse(raw, "dummy");
        Line line = result.getParsedLine();
        long timestamp = line.getTimestamp();
        Assertions.assertTrue(result.isNewLine(), "newLine should be true");
        Assertions.assertEquals(raw, line.getRaw(), "Wrong raw content");
        Assertions.assertEquals(83553234L, timestamp, "Wrong timestamp");
    }
}