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

package de.rochefort.logrifle.data.highlights;

import de.rochefort.logrifle.ui.ColoredString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Highlights {
    private Highlights(){
        throw new IllegalStateException("Don't instantiate me!");
    }

    public static List<ColoredString> applyHighlights(String text, List<Highlight> highlights) {
        List<MatchedSection> matches = new ArrayList<>();
        for (Highlight highlight : highlights) {
            matches = merge(matches, highlight.getMatches(text));
        }
        if (matches.isEmpty()) {
            return Collections.singletonList(new ColoredString(text, null, null));
        }
        matches.sort(Comparator.comparing(MatchedSection::getStartIndex));
        List<ColoredString> result = new ArrayList<>();
        int currentIndex = 0;
        for (MatchedSection match : matches) {
            if (match.getStartIndex() > currentIndex) {
                result.add(new ColoredString(text.substring(currentIndex, match.getStartIndex()), null, null));
            }
            Highlight highlight = match.getHighlight();
            result.add(
                    new ColoredString(
                            text.substring(match.getStartIndex(), match.getEndIndex()),
                            highlight.getFgColor(),
                            highlight.getBgColor()
                    )
            );
            currentIndex = match.getEndIndex();
        }
        if (currentIndex < text.length()) {
            result.add(new ColoredString(text.substring(currentIndex), null, null));
        }
        return result;
    }

    private static List<MatchedSection> merge(List<MatchedSection> existing, List<MatchedSection> toBeAdded) {
        List<MatchedSection> merged = new ArrayList<>(existing);
        for (MatchedSection matchedSection : toBeAdded) {
            merged = merge(merged, matchedSection);
        }
        return merged;
    }


    private static List<MatchedSection> merge(List<MatchedSection> existing, MatchedSection toBeAdded) {
        List<MatchedSection> merged = new ArrayList<>();
        for (MatchedSection matchedSection : existing) {
            if (toBeAdded.contains(matchedSection)) {
                continue;
            }
            if (matchedSection.contains(toBeAdded)) {
                merged.addAll(matchedSection.splitBy(toBeAdded));
            } else if (matchedSection.overlapsWith(toBeAdded)) {
                merged.add(matchedSection.removePartsMatchedBy(toBeAdded));
            } else {
                merged.add(matchedSection);
            }
        }
        merged.add(toBeAdded);
        return merged;
    }

}
