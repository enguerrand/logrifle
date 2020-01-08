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

package de.logrifle.data.parsing;

import com.googlecode.lanterna.TextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Line {
    private int index;
    private final long timestamp;
    private final String raw;
    private final List<String> additionalLines = new ArrayList<>();
    private final String lineLabel;
    private final TextColor labelColor;

    Line(int index, String raw, long timestamp, String lineLabel, TextColor labelColor) {
        this.index = index;
        this.timestamp = timestamp;
        this.raw = raw;
        this.lineLabel = lineLabel;
        this.labelColor = labelColor;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getRaw() {
        return raw;
    }

    public String getLineLabel() {
        return lineLabel;
    }

    public TextColor getLabelColor() {
        return labelColor;
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

    public static Line initialTextLineOf(int index, String raw, String lineLabel, TextColor labelColor) {
        return new Line(index, raw, 0, lineLabel, labelColor);
    }
}
