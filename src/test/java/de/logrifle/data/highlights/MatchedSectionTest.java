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

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MatchedSectionTest {

    @Test
    void removePartsMatchedByAtEnd() {
        Highlight foo = new Highlight("foo", null, null);
        MatchedSection a = new MatchedSection(foo, 12, 16);
        MatchedSection chopped = a.removePartsMatchedBy(new MatchedSection(foo, 14, 18));
        assertEquals(12, chopped.getStartIndex());
        assertEquals(14, chopped.getEndIndex());
    }

    @Test
    void removePartsMatchedByAtStart() {
        Highlight foo = new Highlight("foo", null, null);
        MatchedSection a = new MatchedSection(foo, 12, 16);
        MatchedSection chopped = a.removePartsMatchedBy(new MatchedSection(foo, 4, 14));
        assertEquals(14, chopped.getStartIndex());
        assertEquals(16, chopped.getEndIndex());
    }

    @Test
    void split() {
        Highlight foo = new Highlight("foo", null, null);
        MatchedSection a = new MatchedSection(foo, 12, 18);
        List<MatchedSection> split = a.splitBy(new MatchedSection(foo, 14, 16));
        assertEquals(12, split.get(0).getStartIndex());
        assertEquals(14, split.get(0).getEndIndex());
        assertEquals(16, split.get(1).getStartIndex());
        assertEquals(18, split.get(1).getEndIndex());
    }


}