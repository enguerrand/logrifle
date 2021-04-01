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
import de.logrifle.base.Strings;
import de.logrifle.data.views.DataView;
import de.logrifle.data.views.LineSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Line {
    public static final Comparator<Line> ORDERING_COMPARATOR = Comparator
            .comparing(Line::getDateChangeCount)
            .thenComparing(Line::getTimestamp);
    public static final String EXPORT_LABEL_SEPARATOR = ": ";
    private int index;
    private final long dateChangeCount;
    private final long timestamp;
    private final String raw;
    private final List<String> additionalLines = new CopyOnWriteArrayList<>();
    private final LineSource source;

    Line(int index, String raw, long timestamp, long dateChangeCount, LineSource source) {
        this.index = index;
        this.timestamp = timestamp;
        this.raw = sanitize(raw);
        this.dateChangeCount = dateChangeCount;
        this.source = Objects.requireNonNull(source);
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
        return source.getTitle();
    }

    public boolean isVisible() {
        return source.isActive();
    }

    public boolean belongsTo(LineSource lineSource) {
        return lineSource.equals(this.source);
    }

    public TextColor getLabelColor() {
        return source.getViewColor();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getDateChangeCount() {
        return dateChangeCount;
    }

    public List<String> getAdditionalLines() {
        return additionalLines;
    }

    public void appendAdditionalLine(String text){
        additionalLines.add(sanitize(text));
    }

    public boolean contains(Pattern pattern) {
        if (pattern.matcher(getRaw()).find()) {
            return true;
        }
        return getAdditionalLines().stream()
                .anyMatch(additionalLine -> pattern.matcher(additionalLine).find());
    }

    private String sanitize(String raw) {
        return raw.replaceAll("\t", "    ");
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

    public static Line initialTextLineOf(int index, String raw, DataView source) {
        return new Line(index, raw, 0, 0, source);
    }

    @Override
    public String toString() {
        return "Line{" +
                "raw='" + raw + '\'' +
                '}';
    }

    public Collection<String> export(int wantedLabelLength) {
        List<String> raw = new ArrayList<>();
        raw.add(this.raw);
        raw.addAll(this.additionalLines);
        if (wantedLabelLength == 0) {
            return raw;
        }

        String fullLabel = getLineLabel();
        String label;
        if (fullLabel.length() > wantedLabelLength) {
            label = fullLabel.substring(0, wantedLabelLength);
        } else if (fullLabel.length() < wantedLabelLength) {
            label = Strings.pad(fullLabel, wantedLabelLength, false);
        } else {
            label = fullLabel;
        }
        return raw.stream()
                .map(r -> label + EXPORT_LABEL_SEPARATOR + r)
                .collect(Collectors.toList());
    }
}
