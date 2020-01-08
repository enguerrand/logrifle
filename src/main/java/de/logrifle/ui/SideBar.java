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
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;
import de.logrifle.data.views.ViewsTree;
import de.logrifle.base.Digits;
import de.logrifle.base.Strings;
import de.logrifle.data.highlights.Highlight;
import de.logrifle.data.highlights.HighlightsData;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class SideBar {
    public static final String FILTERS_TITLE = "Views Tree";
    public static final String HIGHLIGHTS_TITLE = "Highlights";
    private final Panel panel;
    private final Panel viewsContentPanel;
    private final Panel highlightsContentPanel;
    private final ViewsTree viewsTree;
    private final HighlightsData highlightsData;
    private final Label filtersTitleLabel;
    private final Label highlightsTitleLabel;

    SideBar(ViewsTree viewsTree, HighlightsData highlightsData) {
        this.viewsTree = viewsTree;
        this.highlightsData = highlightsData;
        this.panel = new Panel(new BorderLayout());
        Panel viewsPanel = new Panel(new BorderLayout());
        filtersTitleLabel = new Label(FILTERS_TITLE);
        filtersTitleLabel.addStyle(SGR.BOLD);
        viewsPanel.addComponent(filtersTitleLabel);
        filtersTitleLabel.setLayoutData(BorderLayout.Location.TOP);
        this.viewsContentPanel = new Panel(new GridLayout(1));
        viewsPanel.addComponent(this.viewsContentPanel);
        this.viewsContentPanel.setLayoutData(BorderLayout.Location.CENTER);
        Panel highlightsPanel = new Panel(new BorderLayout());
        highlightsTitleLabel = new Label(HIGHLIGHTS_TITLE);
        highlightsTitleLabel.addStyle(SGR.BOLD);
        highlightsPanel.addComponent(highlightsTitleLabel);
        this.highlightsContentPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        highlightsPanel.addComponent(this.highlightsContentPanel);
        this.highlightsContentPanel.setLayoutData(BorderLayout.Location.CENTER);
        this.panel.addComponent(viewsPanel);
        this.panel.addComponent(highlightsPanel);
        viewsPanel.setLayoutData(BorderLayout.Location.TOP);
        highlightsPanel.setLayoutData(BorderLayout.Location.BOTTOM);
    }

    Panel getPanel() {
        return panel;
    }

    int update(boolean show, int maxWindowWidth) {
        UI.checkGuiThreadOrThrow();
        int maxSidebarWidth = (int) Math.min(30, ((maxWindowWidth - 2) * 0.5));
        if (show) {
            filtersTitleLabel.setText(FILTERS_TITLE);
            highlightsTitleLabel.setText(HIGHLIGHTS_TITLE);
            updateHighlights(maxSidebarWidth);
            int maxLabelLength = updateViewsTree(maxSidebarWidth);
            return maxLabelLength;
        } else {
            filtersTitleLabel.setText("");
            highlightsTitleLabel.setText("");
            this.highlightsContentPanel.removeAllComponents();
            this.viewsContentPanel.removeAllComponents();
            return 0;
        }
    }

    private int updateViewsTree(int maxWidth) {
        final AtomicInteger maxLength = new AtomicInteger(0);
        final AtomicInteger nodeCount = new AtomicInteger(0);
        this.viewsContentPanel.removeAllComponents();
        this.viewsTree.walk((node, recursionDepth, focused) -> {
            nodeCount.incrementAndGet();
            String prefixText = node.getNavIndex() + ") ";
            String prefix = Strings.pad(prefixText, 5, true);
            String text;
            if (prefix.length() > maxWidth) {
                prefix = truncateString(prefix, maxWidth);
                text = "";
            } else {
                text = truncateString(buildText(node.getTitle(), recursionDepth, maxWidth), maxWidth - prefix.length());
            }
            int length = prefix.length() + text.length();
            maxLength.updateAndGet(prev -> Math.max(prev, length));
            ColoredString navIndex;
            ColoredString title;
            if (focused) {
                navIndex = new ColoredString(prefix, TextColor.ANSI.BLUE, null, SGR.BOLD);
                title = new ColoredString(text, TextColor.ANSI.YELLOW, null, SGR.BOLD);
            } else {
                navIndex = new ColoredString(prefix, TextColor.ANSI.BLUE, null);
                title = new ColoredString(text, null, null);
            }
            Collection<ColoredString> textComponents = Arrays.asList(
                navIndex,
                title
            );
            MultiColoredLabel label = new MultiColoredLabel(textComponents);
            viewsContentPanel.addComponent(label.asComponent());
        });
        return maxLength.get();
    }

    private void updateHighlights(int maxLength) {
        this.highlightsContentPanel.removeAllComponents();
        List<Highlight> highlights = this.highlightsData.getHighlights();
        int digitCount = Digits.getDigitCount(highlights.size());
        for (int i = 0; i < highlights.size(); i++) {
            Highlight highlight = highlights.get(i);
            this.highlightsContentPanel.addComponent(renderHighlight(highlight, i, digitCount, maxLength));
        }
    }

    private String buildText(String title, int recursionDepth, int maxLength) {
        title = truncateString(title, maxLength);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < recursionDepth; i++) {
            sb.append("  ");
        }
        if (recursionDepth > 0) {
            sb.append("-> ");
        }
        sb.append(title);
        return sb.toString();
    }

    private String truncateString(String s, int maxLength) {
        if (s.length() > maxLength) {
            if (maxLength < 3) {
                return "";
            }
            s = s.substring(0, maxLength - 1) + "...";
        }
        return s;
    }

    private Panel renderHighlight(Highlight highlight, int index, int maxIndexDigitCount, int maxLength) {
        GridLayout layoutManager = new GridLayout(1);
        layoutManager.setHorizontalSpacing(0);
        layoutManager.setLeftMarginSize(0);
        layoutManager.setRightMarginSize(0);
        Panel p = new Panel(layoutManager);
        Label l = new Label(truncateString(String.format("%" + maxIndexDigitCount + "d: %s", index, highlight.getRegex()), maxLength));
        if (highlight.getFgColor() != null) {
            l.setForegroundColor(highlight.getFgColor());
        }
        if (highlight.getBgColor() != null) {
            l.setBackgroundColor(highlight.getBgColor());
        }
        for (SGR style : highlight.getStyles()) {
            l.addStyle(style);
        }
        p.addComponent(l);
        return p;
    }
}
