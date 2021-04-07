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

import de.logrifle.base.DirectDispatcher;
import de.logrifle.base.LogDispatcher;
import de.logrifle.data.bookmarks.Bookmarks;
import de.logrifle.data.parsing.Line;
import de.logrifle.data.parsing.TestLinesFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;

class BookmarksViewTest {

    private static final List<Line> LINES = new ArrayList<>();
    private Bookmarks bookmarks;

    @BeforeAll
    static void beforeAll() {
        LINES.addAll(TestLinesFactory.buildTestLines());
    }

    @BeforeEach
    void beforeEach() {
        LogDispatcher testLogDispatcher = new DirectDispatcher();
        bookmarks = new Bookmarks(false, testLogDispatcher);
        bookmarks.toggle(LINES.get(1));
        bookmarks.toggle(LINES.get(3));
        bookmarks.toggle(LINES.get(5));
        bookmarks.toggle(LINES.get(7));
    }

    @ParameterizedTest
    @CsvSource({
            "0,0,4,0",
            "1,0,4,0",
            "1,1,4,0",
            "1,2,4,0",
            "1,7,4,0",
            "0,7,3,1",
            "1,7,3,1",
            "0,7,2,2",
            "2,7,1,3",
            "3,3,1,1",
            "2,4,1,1",
            "2,5,1,2",
            "2,6,1,2",
            "2,6,2,2",
            "2,7,1,3"
    })
    void updateStartIndexIfNeeded(int currentStartIndex, int focusedLineIndex, int visibleBookmarksCount, int expectedStartIndex) {
        int newStartIndex = BookmarksView.updateStartIndexIfNeeded(currentStartIndex, focusedLineIndex, visibleBookmarksCount, new ArrayList<>(bookmarks.getAll()));
        Assertions.assertEquals(expectedStartIndex, newStartIndex);
    }
}