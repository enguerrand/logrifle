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
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;
import de.rochefort.logrifle.base.Digits;
import de.rochefort.logrifle.data.views.ViewsTree;
import de.rochefort.logrifle.data.highlights.Highlight;
import de.rochefort.logrifle.data.highlights.HighlightsData;

import java.util.ArrayList;
import java.util.List;

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

    int update(boolean show) {
        UI.checkGuiThreadOrThrow();
        if (show) {
            filtersTitleLabel.setText(FILTERS_TITLE);
            highlightsTitleLabel.setText(HIGHLIGHTS_TITLE);
            updateHighlights();
            int maxLabelLength = updateViewsTree();
            return maxLabelLength;
        } else {
            filtersTitleLabel.setText("");
            highlightsTitleLabel.setText("");
            this.highlightsContentPanel.removeAllComponents();
            this.viewsContentPanel.removeAllComponents();
            return 0;
        }
    }

    private int updateViewsTree() {
        this.viewsContentPanel.removeAllComponents();
        final List<Label> labels = new ArrayList<>();
        this.viewsTree.walk((node, recursionDepth, focused) -> {
            String text = buildText(node.getTitle(), recursionDepth);
            Label label = new Label(text);
            labels.add(label);
            viewsContentPanel.addComponent(label);
            if (focused) {
                label.setForegroundColor(TextColor.ANSI.YELLOW);
                label.addStyle(SGR.BOLD);
            }
        });
        int maxLength = 0;
        for (Label label : labels) {
            maxLength = Math.max(maxLength, label.getText().length());
        }
        return maxLength;
    }

    private void updateHighlights() {
        this.highlightsContentPanel.removeAllComponents();
        List<Highlight> highlights = this.highlightsData.getHighlights();
        int digitCount = Digits.getDigitCount(highlights.size());
        for (int i = 0; i < highlights.size(); i++) {
            Highlight highlight = highlights.get(i);
            this.highlightsContentPanel.addComponent(renderHighlight(highlight, i, digitCount));
        }
    }

    private String buildText(String title, int recursionDepth) {
        title = truncateString(title);
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

    private String truncateString(String s) {
        if (s.length() > 30) {
            s = s.substring(0, 28) + "...";
        }
        return s;
    }

    private Panel renderHighlight(Highlight highlight, int index, int maxIndexDigitCount) {
        Panel p = new Panel(new GridLayout(1));
        Label l = new Label(truncateString(String.format("%" + maxIndexDigitCount + "d: %s", index, highlight.getRegex())));
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
