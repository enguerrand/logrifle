package de.rochefort.logrifle.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LineParserTimestampedTextImplTest {

    @Test
    void parse() {
        LineParserTimestampedTextImpl parser = new LineParserTimestampedTextImpl();
        String raw = "DEBUG 23:12:33.234 - whatever log message";
        LineParseResult result = parser.parse(raw);
        Line line = result.getParsedLine();
        long timestamp = line.getTimestamp();
        Assertions.assertTrue(result.isNewLine(), "newLine should be true");
        Assertions.assertEquals(raw, line.getRaw(), "Wrong raw content");
        Assertions.assertEquals(79953234L, timestamp, "Wrong timestamp");
    }
}