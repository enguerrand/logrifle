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
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import de.logrifle.base.Digits;
import de.logrifle.base.Strings;
import de.logrifle.data.highlights.Highlight;
import de.logrifle.data.highlights.HighlightsData;
import de.logrifle.data.views.DataView;
import de.logrifle.data.views.ViewsTree;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SideBar {
    private static final String FILES_TITLE = "Open Files";
    private static final String FILTERS_TITLE = "Views Tree";
    private static final String HIGHLIGHTS_TITLE = "Highlights";
    public static final int DEFAULT_MAX_ABSOLUTE_WIDTH = 30;
    public static final double DEFAULT_MAX_RELATIVE_WIDTH = 0.5;
    private final Panel panel;
    private final Panel filesContentPanel;
    private final Panel viewsTreeContentPanel;
    private final Panel highlightsContentPanel;
    private final ViewsTree viewsTree;
    private final HighlightsData highlightsData;
    private final Label filtersTitleLabel;
    private final Label highlightsTitleLabel;
    // only access on ui thread
    private int maxAbsoluteWidth;
    // only access on ui thread
    private double maxRelativeWidth;
    private final GridLayout filesContentLayout;
    private final GridLayout viewsContentLayout;
    private final GridLayout highlightsLayout;

    SideBar(ViewsTree viewsTree, HighlightsData highlightsData, int maxAbsoluteWidth, double maxRelativeWidth) {
        this.maxAbsoluteWidth = maxAbsoluteWidth;
        this.maxRelativeWidth = maxRelativeWidth;
        this.viewsTree = viewsTree;
        this.highlightsData = highlightsData;
        this.panel = new Panel(new BorderLayout());

        Panel openFilesPanel = new Panel(new BorderLayout());
        Label openFilesLabel = new Label(FILES_TITLE);
        openFilesLabel.addStyle(SGR.BOLD);
        openFilesPanel.addComponent(openFilesLabel);
        openFilesLabel.setLayoutData(BorderLayout.Location.TOP);
        filesContentLayout = new ZeroMarginsGridLayout(1);
        filesContentPanel = new Panel(filesContentLayout);
        openFilesPanel.addComponent(filesContentPanel);
        filesContentPanel.setLayoutData(BorderLayout.Location.CENTER);

        Panel filterViewsPanel = new Panel(new BorderLayout());
        filtersTitleLabel = new Label(FILTERS_TITLE);
        filtersTitleLabel.addStyle(SGR.BOLD);
        filterViewsPanel.addComponent(filtersTitleLabel);
        filtersTitleLabel.setLayoutData(BorderLayout.Location.TOP);
        viewsContentLayout = new ZeroMarginsGridLayout(1);
        viewsTreeContentPanel = new Panel(viewsContentLayout);
        filterViewsPanel.addComponent(this.viewsTreeContentPanel);
        viewsTreeContentPanel.setLayoutData(BorderLayout.Location.CENTER);

        Panel highlightsPanel = new Panel(new BorderLayout());
        highlightsTitleLabel = new Label(HIGHLIGHTS_TITLE);
        highlightsTitleLabel.addStyle(SGR.BOLD);
        highlightsPanel.addComponent(highlightsTitleLabel);
        highlightsLayout = new ZeroMarginsGridLayout(1);
        this.highlightsContentPanel = new Panel(highlightsLayout);
        highlightsPanel.addComponent(this.highlightsContentPanel);
        this.highlightsContentPanel.setLayoutData(BorderLayout.Location.CENTER);

        this.panel.addComponent(openFilesPanel);
        this.panel.addComponent(filterViewsPanel);
        this.panel.addComponent(highlightsPanel);
        openFilesPanel.setLayoutData(BorderLayout.Location.TOP);
        filterViewsPanel.setLayoutData(BorderLayout.Location.CENTER);
        highlightsPanel.setLayoutData(BorderLayout.Location.BOTTOM);
        setRightMargin(1);
    }

    private void setRightMargin(int columns) {
        filesContentLayout.setRightMarginSize(columns);
        viewsContentLayout.setRightMarginSize(columns);
        highlightsLayout.setRightMarginSize(columns);
    }

    Panel getPanel() {
        return panel;
    }

    int update(boolean show, int maxWindowWidth) {
        UI.checkGuiThreadOrThrow();
        int maxSidebarWidth = (int) Math.min(maxAbsoluteWidth, (maxWindowWidth * maxRelativeWidth));
        if (show) {
            setRightMargin(Math.min(maxWindowWidth, 1));
            filtersTitleLabel.setText(FILTERS_TITLE);
            highlightsTitleLabel.setText(HIGHLIGHTS_TITLE);
            updateHighlights(maxSidebarWidth);
            int maxLabelLengthViewsTree = updateViewsTree(maxSidebarWidth);
            int maxLabelLengthSourceViewsList = updateFilesListView(maxSidebarWidth);
            return Math.max(maxLabelLengthViewsTree, maxLabelLengthSourceViewsList);
        } else {
            setRightMargin(0);
            filtersTitleLabel.setText("");
            highlightsTitleLabel.setText("");
            this.highlightsContentPanel.removeAllComponents();
            this.viewsTreeContentPanel.removeAllComponents();
            return 0;
        }
    }

    private int updateFilesListView(int maxWidth) {
        final AtomicInteger maxLength = new AtomicInteger(0);
        this.filesContentPanel.removeAllComponents();
        List<DataView> views = this.viewsTree.getViews();
        for (int i = 0; i < views.size(); i++) {
            DataView view = views.get(i);
            String prefixText = i + ") ";
            String prefix = Strings.pad(prefixText, 5, true);
            String text;
            if (prefix.length() > maxWidth) {
                prefix = Strings.truncateString(prefix, maxWidth);
                text = "";
            } else {
                text = Strings.truncateString(view.getTitle(), maxWidth - prefix.length());
            }
            int length = prefix.length() + text.length();
            maxLength.updateAndGet(prev -> Math.max(prev, length));
            TextColor viewColor = view.getViewColor();
            SGR[] styles = view.isActive() ? new SGR[]{SGR.BOLD} : new SGR[0];
            ColoredString navIndex = new ColoredString(prefix, viewColor, null);
            ColoredString title = new ColoredString(text, TextColor.ANSI.WHITE, null, styles);
            Collection<ColoredString> textComponents = Arrays.asList(
                    navIndex,
                    title
            );
            MultiColoredLabel label = new MultiColoredLabel(textComponents);
            filesContentPanel.addComponent(label.asComponent());
        }
        return maxLength.get();
    }

    private int updateViewsTree(int maxWidth) {
        final AtomicInteger maxLength = new AtomicInteger(0);
        final AtomicInteger nodeCount = new AtomicInteger(0);
        this.viewsTreeContentPanel.removeAllComponents();
        this.viewsTree.walk((node, recursionDepth, focused) -> {
            nodeCount.incrementAndGet();
            String prefixText = node.getNavIndex() + ") ";
            String prefix = Strings.pad(prefixText, 5, true);
            String text;
            if (prefix.length() > maxWidth) {
                prefix = Strings.truncateString(prefix, maxWidth);
                text = "";
            } else {
                text = Strings.truncateString(buildText(node.getTitle(), recursionDepth, maxWidth), maxWidth - prefix.length());
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
            viewsTreeContentPanel.addComponent(label.asComponent());
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
        title = Strings.truncateString(title, maxLength);
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

    private Panel renderHighlight(Highlight highlight, int index, int maxIndexDigitCount, int maxLength) {
        GridLayout layoutManager = new ZeroMarginsGridLayout(1);
        Panel p = new Panel(layoutManager);
        Label l = new Label(Strings.truncateString(String.format("%" + maxIndexDigitCount + "d: %s", index, highlight.getRegex()), maxLength));
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

    public void setMaxAbsoluteWidth(int maxAbsoluteWidth) {
        UI.checkGuiThreadOrThrow();
        this.maxAbsoluteWidth = maxAbsoluteWidth;
    }

    public void setMaxRelativeWidth(double maxRelativeWidth) {
        UI.checkGuiThreadOrThrow();
        this.maxRelativeWidth = maxRelativeWidth;
    }
}
