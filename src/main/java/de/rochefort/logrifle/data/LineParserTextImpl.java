package de.rochefort.logrifle.data;

public class LineParserTextImpl implements LineParser {
    @Override
    public Line parse(String raw) {
        return new Line(raw);
    }
}
