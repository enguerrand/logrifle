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

package de.logrifle.ui;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.AbstractComponent;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import de.logrifle.base.Digits;
import de.logrifle.base.Strings;
import de.logrifle.data.bookmarks.Bookmarks;
import de.logrifle.data.highlights.Highlight;
import de.logrifle.data.highlights.Highlights;
import de.logrifle.data.parsing.Line;

import java.util.ArrayList;
import java.util.List;

public class DefaultLogLineRenderer implements LogLineRenderer {
    private static final String ADDITIONAL_LINE_INDENT = "    ";
    @Override
    public AbstractComponent<?> render(
            Line line,
            int totalLineCount,
            boolean focused,
            int lineLabelLength,
            int beginColumn,
            List<Highlight> highlights,
            Bookmarks bookmarks,
            boolean lineIndexHot,
            LineDetailViewState lineDetailViewState,
            int maxRenderableLineCount,
            boolean showLineNumber
    ) {
        int digitCount = Digits.getDigitCount(totalLineCount);
        String lineLabel = "";
        if (lineLabelLength > 0) {
            String fullLabel = line.getLineLabel();
            lineLabel = fullLabel.substring(0, Math.min(lineLabelLength, fullLabel.length()));
        }
        if (lineLabel.length() < lineLabelLength + 1) {
            lineLabel = Strings.pad(lineLabel, lineLabelLength + 1, false);
        }
        String lineText = line.getRaw();
        lineText = getScrolledString(beginColumn, lineText);
        List<ColoredString> coloredStrings = new ArrayList<>();
        coloredStrings.add(new ColoredString(lineLabel, line.getLabelColor(), null));
        boolean bookmarked = bookmarks.isLineBookmarked(line);
        if (showLineNumber) {
            coloredStrings.addAll(
                    buildLineNumber(line, lineIndexHot, digitCount, bookmarked)
            );
        }
        if (focused) {
            coloredStrings.add(new ColoredString(lineText, TextColor.ANSI.WHITE, null, SGR.BOLD));
        } else {
            coloredStrings.addAll(Highlights.applyHighlights(lineText, highlights));
        }
        Panel wrapper = new Panel(new ZeroMarginsGridLayout(1));
        if (lineDetailViewState.isMainLineVisible(line)) {
            wrapper.addComponent(new MultiColoredLabel(coloredStrings).asComponent());
        }
        List<String> additionalLinesToRender = lineDetailViewState.getAdditionalLinesToRender(line, maxRenderableLineCount);
        for (String additionalLine : additionalLinesToRender) {
            String scrolledAdditionalLine = getScrolledString(beginColumn, ADDITIONAL_LINE_INDENT + additionalLine);
            String displayedText = Strings.pad(
                    scrolledAdditionalLine,
                    lineLabelLength + digitCount + 2 + scrolledAdditionalLine.length(),
                    true
            );
            Label additionalLineLabel = new Label(displayedText);
            additionalLineLabel.setLabelWidth(null);
            wrapper.addComponent(additionalLineLabel);
        }
        return wrapper;
    }

    private List<ColoredString> buildLineNumber(Line line, boolean lineIndexHot, int digitCount, boolean bookmarked) {
        List<ColoredString> coloredStrings = new ArrayList<>();
        TextColor.ANSI lineNumberColor;
        if (lineIndexHot) {
            lineNumberColor = TextColor.ANSI.RED;
        } else if (line.getAdditionalLines().isEmpty()) {
            lineNumberColor = TextColor.ANSI.CYAN;
        } else {
            lineNumberColor = TextColor.ANSI.MAGENTA;
        }
        coloredStrings.add(new ColoredString(String.format("%" + digitCount + "d", line.getIndex()), lineNumberColor, bookmarked ? TextColor.ANSI.RED: null));
        coloredStrings.add(new ColoredString(" ", null, null));
        return coloredStrings;
    }

    private String getScrolledString(int beginColumn, String full) {
        if (full.length() < beginColumn) {
            full = "";
        } else {
            full = full.substring(beginColumn);
        }
        return full;
    }
}
