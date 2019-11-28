package de.rochefort.logrifle.data;

import java.util.Objects;

public class Line {
    private final String raw;

    Line(String raw) {
        this.raw = raw;
    }

    public String getRaw() {
        return raw;
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
