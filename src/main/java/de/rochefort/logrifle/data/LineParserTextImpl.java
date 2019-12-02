package de.rochefort.logrifle.data;

public class LineParserTextImpl implements LineParser {
    @Override
    public LineParseResult parse(String raw) {
        return new LineParseResult(new Line(raw, System.currentTimeMillis()));
    }
}
