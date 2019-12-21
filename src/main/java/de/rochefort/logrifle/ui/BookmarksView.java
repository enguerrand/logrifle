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

    void update(boolean shown, int totalLinesCount, int beginColumn, List<Highlight> highlights, TerminalSize availableSpace) {
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

            int index = 0;
            for (Bookmark bookmark : bookmarks.getAll()) {
                if (startAtIndex > index) {
                    continue;
                }
                if (index >= availableSpace.getRows() - TITLE_HEIGHT) {
                    break;
                }
                index++;
                Line line = bookmark.getLine();
                AbstractComponent<?> bookmarkComponent = logLineRenderer.render(line, totalLinesCount, false, 0, beginColumn, highlights, this.bookmarks);
                bookmarksPanel.addComponent(bookmarkComponent);
            }
        }
    }

    public Panel getPanel() {
        return panel;
    }
}