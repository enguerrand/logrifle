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

package de.rochefort.logrifle.ui;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.AbstractComponent;
import de.rochefort.logrifle.base.Digits;
import de.rochefort.logrifle.data.parsing.Line;
import de.rochefort.logrifle.data.highlights.Highlight;
import de.rochefort.logrifle.data.highlights.Highlights;

import java.util.ArrayList;
import java.util.List;

public class DefaultLogLineRenderer implements LogLineRenderer {
    @Override
    public AbstractComponent<?> render(
            Line line,
            int totalLineCount,
            boolean focused,
            int lineLabelLength,
            int beginColumn,
            List<Highlight> highlights
    ) {
        int digitCount = Digits.getDigitCount(totalLineCount);
        String lineLabel = "";
        if (lineLabelLength > 0) {
            String fullLabel = line.getLineLabel();
            lineLabel = fullLabel.substring(0, Math.min(lineLabelLength, fullLabel.length()));
        }
        String lineText = line.getRaw();
        if (lineText.length() < beginColumn) {
            lineText = "";
        } else {
            lineText = lineText.substring(beginColumn);
        }
        List<ColoredString> coloredStrings = new ArrayList<>();
        coloredStrings.add(new ColoredString(lineLabel, line.getLabelColor(), null));
        coloredStrings.add(new ColoredString(String.format(" %" + digitCount + "d ", line.getIndex()), TextColor.ANSI.CYAN, null));
        if (focused) {
            coloredStrings.add(new ColoredString(lineText, TextColor.ANSI.WHITE, null, SGR.BOLD));
        } else {
            coloredStrings.addAll(Highlights.applyHighlights(lineText, highlights));
        }
        return new MultiColoredLabel(coloredStrings).asComponent();
    }
}
