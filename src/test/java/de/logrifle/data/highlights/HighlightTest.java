/*
 *  Copyright 2020, Enguerrand de Rochefort
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

import com.googlecode.lanterna.TextColor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HighlightTest {
    private Highlight highlightRed = new Highlight("foobar", TextColor.ANSI.RED, null);

    @Test
    void noMatch() {
        String text = "Foo bar bas";
        List<MatchedSection> matches = highlightRed.getMatches(text);
        assertEquals(0, matches.size());
    }

    @Test
    void oneRedMatch() {
        String text = "Foo foobar bas";
        List<MatchedSection> matches = highlightRed.getMatches(text);
        assertEquals(1, matches.size());
        MatchedSection matchedSection = matches.get(0);
        assertEquals(TextColor.ANSI.RED, matchedSection.getHighlight().getFgColor());
        assertEquals(4, matchedSection.getStartIndex());
        assertEquals(10, matchedSection.getEndIndex());
    }

    @Test
    void twoSeperateRedMatches() {
        String text = "Foo foobar bas foobar bar";
        List<MatchedSection> matches = highlightRed.getMatches(text);
        assertEquals(2, matches.size());
        assertEquals(TextColor.ANSI.RED, matches.get(0).getHighlight().getFgColor());
        assertEquals(4, matches.get(0).getStartIndex());
        assertEquals(10, matches.get(0).getEndIndex());
        assertEquals(TextColor.ANSI.RED, matches.get(1).getHighlight().getFgColor());
        assertEquals(15, matches.get(1).getStartIndex());
        assertEquals(21, matches.get(1).getEndIndex());
    }
}