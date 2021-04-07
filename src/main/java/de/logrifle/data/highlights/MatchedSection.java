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

package de.logrifle.data.highlights;

import java.util.Arrays;
import java.util.List;

public class MatchedSection {
    private final Highlight highlight;
    private final int startIndex;
    private final int endIndex;

    public MatchedSection(Highlight highlight, int startIndex, int endIndex) {
        this.highlight = highlight;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public Highlight getHighlight() {
        return highlight;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public boolean overlapsWith(MatchedSection other) {
        if (contains(other) || other.contains(this)) {
            return true;
        }
        return getStartIndex() < other.getEndIndex()
                && getEndIndex() > other.getStartIndex();
    }

    public boolean contains(MatchedSection other) {
        return (getStartIndex() <= other.getStartIndex()
                && getEndIndex() >= other.getEndIndex());
    }

    public List<MatchedSection> splitBy(MatchedSection other) {
        if (!contains(other)) {
            throw new IllegalArgumentException("section cannot be split by other section which is not contained!");
        }
        return Arrays.asList(
                new MatchedSection(this.highlight, startIndex, other.startIndex),
                new MatchedSection(this.highlight, other.endIndex, endIndex)
        );
    }

    public MatchedSection removePartsMatchedBy(MatchedSection other) {
        if (other.contains(this)) {
            throw new IllegalArgumentException("This operation would remove everything!");
        }
        if (other.getEndIndex() >= startIndex && other.getStartIndex() < startIndex) {
            return new MatchedSection(this.highlight, other.getEndIndex(), endIndex);
        }
        if (other.getStartIndex() <= endIndex && other.getEndIndex() > startIndex) {
            return new MatchedSection(this.highlight, startIndex, other.getStartIndex());
        }
        throw new IllegalArgumentException("No overlap or split");
    }

}
