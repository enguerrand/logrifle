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
import com.googlecode.lanterna.gui2.Label;
import de.rochefort.logrifle.data.parsing.Line;

public class DefaultLogLineRenderer implements LogLineRenderer {
    @Override
    public AbstractComponent<?> render(Line line, int lineIndex, int visibleLineCount, boolean focused, int lineLabelLength) {
        int digitCount = getDigitCount(visibleLineCount);
        String lineLabel = "";
        if (lineLabelLength > 0) {
            String fullLabel = line.getLineLabel();
            lineLabel = fullLabel.substring(0, Math.min(lineLabelLength, fullLabel.length())) + "| ";
        }
        Label label = new Label(String.format("%s%" + digitCount + "d %s", lineLabel, lineIndex, line.getRaw()));
        if (focused) {
            label.setForegroundColor(TextColor.ANSI.WHITE);
            label.addStyle(SGR.BOLD);
        }
        return label;
    }

    private int getDigitCount(int n) {
        int count = 0;
        while (n != 0) {
            n = n / 10;
            ++count;
        }
        return count;
    }
}
