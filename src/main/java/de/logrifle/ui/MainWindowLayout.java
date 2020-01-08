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

import com.googlecode.lanterna.TerminalSize;

class MainWindowLayout {
    private static final double MAX_BOOKMARKS_RATIO = 0.5;
    private final TerminalSize logViewSize;
    private final TerminalSize commandBarSize;
    private final TerminalSize bookmarksSize;

    private MainWindowLayout(TerminalSize logViewSize, TerminalSize commandBarSize, TerminalSize bookmarksSize) {
        this.logViewSize = logViewSize;
        this.commandBarSize = commandBarSize;
        this.bookmarksSize = bookmarksSize;
    }

    TerminalSize getLogViewSize() {
        return logViewSize;
    }

    TerminalSize getCommandBarSize() {
        return commandBarSize;
    }

    public TerminalSize getBookmarksSize() {
        return bookmarksSize;
    }

    static MainWindowLayout compute(TerminalSize terminalSize, int commandBarHeight, int sideBarWidth, int bookmarksCount, boolean bookmarksViewVisible) {
        if (sideBarWidth >= terminalSize.getColumns()) {
            throw new IllegalStateException("Side bar is too large: "+sideBarWidth+" >= " + terminalSize.getColumns());
        }
        TerminalSize cmd = new TerminalSize(terminalSize.getColumns() , commandBarHeight);
        int rowsMinusCmd = terminalSize.getRows() - cmd.getRows();
        int logViewWidth = terminalSize.getColumns() - sideBarWidth;
        TerminalSize bm = bookmarksViewVisible
                ? computeBookmarksSize(logViewWidth, bookmarksCount, rowsMinusCmd)
                : new TerminalSize(logViewWidth, 0);
        TerminalSize log = new TerminalSize(logViewWidth, rowsMinusCmd - bm.getRows());
        return new MainWindowLayout(log, cmd, bm);
    }

    static TerminalSize computeBookmarksSizeFrom(TerminalSize logViewSize, int mainWindowHeight, int cmdBarHeight, int bookmarksCount) {
        int rowsMinusCmd = mainWindowHeight - logViewSize.getRows() - cmdBarHeight;
        return computeBookmarksSize(logViewSize.getColumns(), bookmarksCount, rowsMinusCmd);
    }

    private static TerminalSize computeBookmarksSize(int logViewWidth, int bookmarksCount, int rowsMinusCmd) {
        int bookmarksHeight;
        if (rowsMinusCmd < BookmarksView.TITLE_HEIGHT) {
            bookmarksHeight = 0;
        } else {
            bookmarksHeight = Math.min(bookmarksCount + BookmarksView.TITLE_HEIGHT, (int)(rowsMinusCmd * MAX_BOOKMARKS_RATIO));
        }
        return new TerminalSize(logViewWidth, bookmarksHeight);
    }
}