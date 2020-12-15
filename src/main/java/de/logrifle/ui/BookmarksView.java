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
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.AbstractComponent;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import de.logrifle.base.Strings;
import de.logrifle.data.bookmarks.Bookmark;
import de.logrifle.data.bookmarks.Bookmarks;
import de.logrifle.data.highlights.Highlight;
import de.logrifle.data.parsing.Line;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class BookmarksView {
    static final int TITLE_HEIGHT = 1;
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

    void update(boolean shown, int totalLinesCount, int beginColumn, List<Highlight> highlights, TerminalSize availableSpace, int globalFocusedLineIndex, int lineLabelLength, boolean showLineNumbers) {
        int maxRowsCount = availableSpace.getRows();
        if (!shown || maxRowsCount < TITLE_HEIGHT) {
            panel.removeAllComponents();
        } else {
            String title = "=== Bookmarks ";
            String padded = Strings.pad(title, Math.max(0, availableSpace.getColumns() - 1), "=", false);
            Label titleLabel = new SanitizedLabel(padded);
            titleLabel.addStyle(SGR.BOLD);

            panel.addComponent(titleLabel);
            titleLabel.setLayoutData(BorderLayout.Location.TOP);
            Panel bookmarksPanel = new Panel(new ZeroMarginsGridLayout(1));
            panel.addComponent(bookmarksPanel);
            bookmarksPanel.setLayoutData(BorderLayout.Location.BOTTOM);
            ArrayList<Bookmark> bookmarkArrayList = new ArrayList<>(bookmarks.getAll());
            this.startAtIndex = updateStartIndexIfNeeded(
                    this.startAtIndex,
                    globalFocusedLineIndex,
                    maxRowsCount - TITLE_HEIGHT,
                    bookmarkArrayList
            );

            for (int i = 0; i < bookmarkArrayList.size(); i++) {
                Bookmark bookmark = bookmarkArrayList.get(i);
                if (startAtIndex > i) {
                    continue;
                }
                if (i - startAtIndex >= maxRowsCount - TITLE_HEIGHT) {
                    break;
                }
                Line line = bookmark.getLine();
                AbstractComponent<?> bookmarkComponent = logLineRenderer.render(
                        line,
                        totalLinesCount,
                        line.getIndex() == globalFocusedLineIndex,
                        lineLabelLength,
                        beginColumn,
                        highlights,
                        this.bookmarks,
                        false,
                        LineDetailViewState.IGNORED,
                        maxRowsCount,
                        showLineNumbers
                );
                bookmarksPanel.addComponent(bookmarkComponent);
            }
        }
    }

    /**
     * package private for testing only
     */
    static int updateStartIndexIfNeeded(int currentStartIndex, int globalFocusedLineIndex, int visibleBookmarksCount, List<Bookmark> allBookmarks) {
        List<Integer> mustSees = computeMustSees(allBookmarks, globalFocusedLineIndex);
        if (mustSees.isEmpty()) {
            return currentStartIndex;
        }
        int updatedIndex = ensureVisible(currentStartIndex, visibleBookmarksCount, mustSees.get(0));
        if (visibleBookmarksCount > 1 && mustSees.size() > 1) {
            updatedIndex = ensureVisible(updatedIndex, visibleBookmarksCount, mustSees.get(1));
        }
        if (allBookmarks.size() - updatedIndex < visibleBookmarksCount) {
            updatedIndex = Math.max(0, allBookmarks.size() - visibleBookmarksCount);
        }
        return updatedIndex;
    }

    private static int ensureVisible(int currentStartIndex, int rows, int index) {
        if (index < currentStartIndex) {
            return index;
        } else if (index >= currentStartIndex + rows) {
            return index - rows + 1;
        } else {
            return currentStartIndex;
        }
    }

    private static List<Integer> computeMustSees(List<Bookmark> allBookmarks, int globalFocusedLineIndex) {
        if (allBookmarks.isEmpty()) {
            return Collections.emptyList();
        }
        Bookmark last = null;
        List<Integer> mustSees = new ArrayList<>();
        for (int i = 0; i < allBookmarks.size(); i++) {
            Bookmark bookmark = allBookmarks.get(i);

            int currentIndex = bookmark.getLine().getIndex();
            if (currentIndex == globalFocusedLineIndex) {
                mustSees.add(i);
                break;
            }
            if (currentIndex > globalFocusedLineIndex && (last == null || last.getLine().getIndex() < currentIndex )) {
                mustSees.add(i);
                if (last != null) {
                    mustSees.add(i-1);
                }
                break;
            }
            last = bookmark;
        }
        if (mustSees.isEmpty()) {
            mustSees.add(allBookmarks.size() -1);
        }
        mustSees.sort(Comparator.naturalOrder());
        return mustSees;
    }

    Panel getPanel() {
        return panel;
    }
}