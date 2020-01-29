/*
 *  Copyright 2020, Enguerrand de Rochefort
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MainWindowLayoutTest {

    @ParameterizedTest
    @CsvSource({
            "80,30,1,25,10,true,11",
            "80,30,0,25,10,true,11",
            "70,30,1,25,30,true,14",
            "70,30,0,25,30,true,15",
    })
    void compute(int terminalSizeCols, int terminalSizeRows, int commandBarHeight, int sideBarWidth, int bookmarksCount, boolean bookmarksVisible, int expectedBookmarksHeight) {
        TerminalSize terminalSize = new TerminalSize(terminalSizeCols, terminalSizeRows);
        MainWindowLayout layout = MainWindowLayout.compute(terminalSize, commandBarHeight, sideBarWidth, bookmarksCount, bookmarksVisible);
        TerminalSize bookmarksSize = layout.getBookmarksSize();
        TerminalSize commandBarSize = layout.getCommandBarSize();
        TerminalSize logViewSize = layout.getLogViewSize();

        Assertions.assertEquals(terminalSizeCols, commandBarSize.getColumns(), "wrong command bar width");
        Assertions.assertEquals(commandBarHeight, commandBarSize.getRows(), "wrong command bar height");

        Assertions.assertEquals(terminalSizeCols - sideBarWidth, logViewSize.getColumns(), "wrong log view width");
        Assertions.assertEquals(terminalSizeCols - sideBarWidth, bookmarksSize.getColumns(), "wrong bookmarks view width");

        Assertions.assertEquals(expectedBookmarksHeight, bookmarksSize.getRows(), "wrong bookmarks view height");
        Assertions.assertEquals(terminalSizeRows - expectedBookmarksHeight - commandBarHeight, logViewSize.getRows(), "wrong log view height");
    }

    @ParameterizedTest
    @CsvSource({
            "35,70,1,0,1",
            "35,60,0,29,30",
            "35,60,1,29,29",
            "35,59,0,29,29",
            "35,58,1,29,28",
    })
    void computeBookmarksSizeFrom(int logViewPanelWidth, int mainWindowHeight, int commandBarHeight, int bookmarksCount, short expectedHeight) {
        TerminalSize bookmarksSize = MainWindowLayout.computeBookmarksSizeFrom(logViewPanelWidth, mainWindowHeight, commandBarHeight, bookmarksCount);

        Assertions.assertEquals(logViewPanelWidth, bookmarksSize.getColumns(), "Wrong bookmarks view width");
        Assertions.assertEquals(expectedHeight, bookmarksSize.getRows(), "Wrong bookmarks view height");
    }
}