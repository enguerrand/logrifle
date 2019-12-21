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
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.AbstractComponent;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import de.rochefort.logrifle.base.Strings;
import de.rochefort.logrifle.data.bookmarks.Bookmark;
import de.rochefort.logrifle.data.bookmarks.Bookmarks;
import de.rochefort.logrifle.data.highlights.Highlight;
import de.rochefort.logrifle.data.parsing.Line;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class BookmarksView {
    public static final int TITLE_HEIGHT = 1;
    private final Bookmarks bookmarks;
    private final LogLineRenderer logLineRenderer;
    private final Panel panel;
    private int startAtIndex = 0;

    BookmarksView(Bookmarks bookmarks, LogLineRenderer logLineRenderer) {
        this.bookmarks = bookmarks;
        this.logLineRenderer = logLineRenderer;
        BorderLayout layout = new BorderLayout();
        panel = new Panel(layout);
    }

    void update(boolean shown, int totalLinesCount, int beginColumn, List<Highlight> highlights, TerminalSize availableSpace, int focusedLineIndex, int lineLabelLength) {
        if (!shown || availableSpace.getRows() < TITLE_HEIGHT) {
            panel.removeAllComponents();
        } else {
            String title = "=== Bookmarks ";
            String padded = Strings.pad(title, Math.max(0, availableSpace.getColumns() - 3), "=");
            Label titleLabel = new Label(padded);
            titleLabel.addStyle(SGR.BOLD);

            panel.addComponent(titleLabel);
            titleLabel.setLayoutData(BorderLayout.Location.TOP);
            Panel bookmarksPanel = new Panel(new GridLayout(1));
            panel.addComponent(bookmarksPanel);
            bookmarksPanel.setLayoutData(BorderLayout.Location.BOTTOM);

            updateStartIndexIfNeeded(focusedLineIndex, availableSpace.getRows() - TITLE_HEIGHT);

            ArrayList<Bookmark> bookmarkArrayList = new ArrayList<>(bookmarks.getAll());
            for (int i = 0; i < bookmarkArrayList.size(); i++) {
                Bookmark bookmark = bookmarkArrayList.get(i);
                if (startAtIndex > i) {
                    continue;
                }
                if (i - startAtIndex >= availableSpace.getRows() - TITLE_HEIGHT) {
                    break;
                }
                Line line = bookmark.getLine();
                AbstractComponent<?> bookmarkComponent = logLineRenderer.render(line, totalLinesCount, line.getIndex() == focusedLineIndex, lineLabelLength, beginColumn, highlights, this.bookmarks);
                bookmarksPanel.addComponent(bookmarkComponent);
            }
        }
    }

    private void updateStartIndexIfNeeded(int focusedLineIndex, int visibleBookmarksCount) {
        List<Integer> mustSees = computeMustSees(focusedLineIndex);
        if (mustSees.isEmpty()) {
            return;
        }
        ensureVisible(visibleBookmarksCount, mustSees.get(0));
        if (visibleBookmarksCount == 1 || mustSees.size() == 1) {
            return;
        }
        ensureVisible(visibleBookmarksCount, mustSees.get(1));

    }

    private void ensureVisible(int rows, int index) {
        if (index < startAtIndex) {
            startAtIndex = index;
        } else if (index >= startAtIndex + rows) {
            startAtIndex = index - rows + 1;
        }
    }

    private List<Integer> computeMustSees(int focusedLineIndex) {
        ArrayList<Bookmark> bookmarksAsList = new ArrayList<>(this.bookmarks.getAll());
        if (bookmarksAsList.isEmpty()) {
            return Collections.emptyList();
        }
        Bookmark last = null;
        List<Integer> mustSees = new ArrayList<>();
        for (int i = 0; i < bookmarksAsList.size(); i++) {
            Bookmark bookmark = bookmarksAsList.get(i);

            int currentIndex = bookmark.getLine().getIndex();
            if (currentIndex == focusedLineIndex) {
                mustSees.add(i);
                break;
            }
            if (currentIndex > focusedLineIndex && (last == null || last.getLine().getIndex() < currentIndex )) {
                mustSees.add(i);
                if (last != null) {
                    mustSees.add(i-1);
                }
                break;
            }
            last = bookmark;
        }
        if (mustSees.isEmpty()) {
            mustSees.add(bookmarksAsList.size() -1);
        }
        mustSees.sort(Comparator.naturalOrder());
        return mustSees;
    }

    public Panel getPanel() {
        return panel;
    }
}