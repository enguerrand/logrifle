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
import de.logrifle.data.views.UserInputProcessingFailedException;
import de.logrifle.ui.ColoredString;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HighlightsTest {

    @Test
    void applyHighlights() throws UserInputProcessingFailedException {
        Highlight highlightRed = new Highlight("red part", TextColor.ANSI.RED, null);
        Highlight highlightBlue = new Highlight("blue part", TextColor.ANSI.BLUE, null);
        Highlight highlightGreen = new Highlight(" part is", TextColor.ANSI.GREEN, null);
        List<Highlight> highlights = Arrays.asList(
                highlightRed, highlightBlue, highlightGreen
        );
        String text = "This is a boring text, with a red part and a blue part to test highlights. The second " +
                "blue part is overlapped by a green part.";
        List<ColoredString> coloredStrings = Highlights.applyHighlights(text, highlights);
        assertEquals(8, coloredStrings.size());
        checkColoredString(coloredStrings.get(0), null, 30);
        checkColoredString(coloredStrings.get(1), TextColor.ANSI.RED, 8);
        checkColoredString(coloredStrings.get(2), null, 7);
        checkColoredString(coloredStrings.get(3), TextColor.ANSI.BLUE, 9);
        checkColoredString(coloredStrings.get(4), null, 32);
        checkColoredString(coloredStrings.get(5), TextColor.ANSI.BLUE, 4);
        checkColoredString(coloredStrings.get(6), TextColor.ANSI.GREEN, 8);
        checkColoredString(coloredStrings.get(7), null, 28);
        assertEquals(text.length(), coloredStrings.stream().mapToInt(s -> s.getText().length()).sum());
    }

    private void checkColoredString(ColoredString coloredString, TextColor expectedColor, int expectedLength) {
        assertEquals(expectedColor, coloredString.getFgColor().orElse(null));
        assertEquals(expectedLength, coloredString.getText().length());
    }
}