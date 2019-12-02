package de.rochefort.logrifle.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LineParserTimestampedTextImplTest {

    @Test
    void parse() {
        LineParserTimestampedTextImpl parser = new LineParserTimestampedTextImpl(".*(\\d{2}:\\d{2}:\\d{2}\\.\\d{3}).*", "HH:mm:ss.SSS");
        String raw = "DEBUG 23:12:33.234 - whatever log message";
        Line line = parser.parse(raw);
        long timestamp = line.getTimestamp();
        Assertions.assertEquals(raw, line.getRaw(), "Wrong raw content");
        Assertions.assertEquals(79953234L, timestamp, "Wrong timestamp");
    }
}