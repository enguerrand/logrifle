/*
 *  Copyright 2019, Enguerrand de Rochefort
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
