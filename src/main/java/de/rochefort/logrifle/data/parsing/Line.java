package de.rochefort.logrifle.data.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Line {
    private final long timestamp;
    private final String raw;
    private final List<String> additionalLines = new ArrayList<>();

    Line(String raw, long timestamp) {
        this.timestamp = timestamp;
        this.raw = raw;
    }

    public String getRaw() {
        return raw;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<String> getAdditionalLines() {
        return additionalLines;
    }

    public void appendAdditionalLine(String text){
        additionalLines.add(text);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Line line = (Line) o;
        return Objects.equals(raw, line.raw);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raw);
    }
}
