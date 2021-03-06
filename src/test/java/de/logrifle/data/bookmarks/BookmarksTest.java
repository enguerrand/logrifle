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

package de.logrifle.data.bookmarks;

import de.logrifle.base.DirectDispatcher;
import de.logrifle.base.LogDispatcher;
import de.logrifle.data.parsing.Line;
import de.logrifle.data.parsing.TestLinesFactory;
import de.logrifle.ui.LineLabelDisplayMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BookmarksTest {
    private static final List<Line> LINES = new ArrayList<>();
    private static final Charset charset = StandardCharsets.UTF_8;
    private LogDispatcher testLogDispatcher;
    private List<Bookmark> removedBookmarks;
    private List<Bookmark> addedBookmarks;
    private boolean forcedDisplay;
    private BookmarksListener listener;

    @BeforeAll
    static void beforeAll() {
        LINES.addAll(TestLinesFactory.buildTestLines());
    }

    @BeforeEach
    void setUp() {
        testLogDispatcher = new DirectDispatcher();
        addedBookmarks = new ArrayList<>();
        removedBookmarks = new ArrayList<>();
        forcedDisplay = false;
        listener = new BookmarksListener() {
            @Override
            public void added(Bookmarks source, Collection<Bookmark> added) {
                addedBookmarks.addAll(added);
            }

            @Override
            public void removed(Bookmarks source, Collection<Bookmark> removed) {
                removedBookmarks.addAll(removed);
            }

            @Override
            public void forcedDisplayChanged(Bookmarks source) {
                forcedDisplay = source.isBookmarksDisplayForced();
            }
        };
    }

    @Test
    void toggleBookmark() {
        Bookmarks bookmarks = new Bookmarks(false, testLogDispatcher);
        bookmarks.addListener(listener);
        Line lineToBookmark = LINES.get(3);
        bookmarks.toggle(lineToBookmark);
        Assertions.assertEquals(1, bookmarks.count());
        Assertions.assertTrue(bookmarks.isLineBookmarked(lineToBookmark));
        Bookmark expectedBookmark = new Bookmark(lineToBookmark);
        Assertions.assertTrue(bookmarks.getAll().contains(expectedBookmark));
        Assertions.assertEquals(Collections.singletonList(expectedBookmark), this.addedBookmarks);
        bookmarks.toggle(lineToBookmark);
        Assertions.assertEquals(0, bookmarks.count());
        Assertions.assertFalse(bookmarks.isLineBookmarked(lineToBookmark));
        Assertions.assertEquals(Collections.singletonList(expectedBookmark), this.removedBookmarks);
    }

    @ParameterizedTest
    @CsvSource({
            "0,5,2",
            "1,5,2",
            "2,5,4",
            "3,2,4",
            "4,2,5",
            "5,4,2",
            "6,5,2",
            "7,5,2",
            "8,5,2",
    })
    void findBookmark(String fromIndex, String expectedPreviousResultIndex, String expectedNextResultIndex) {
        Bookmarks bookmarks = new Bookmarks(false, testLogDispatcher);
        Line first = LINES.get(2);
        Line second = LINES.get(4);
        Line third = LINES.get(5);
        bookmarks.toggle(first);
        bookmarks.toggle(second);
        bookmarks.toggle(third);
        Bookmark next = bookmarks.findNext(Integer.parseInt(fromIndex)).orElse(null);
        Bookmark previous = bookmarks.findPrevious(Integer.parseInt(fromIndex)).orElse(null);
        Assertions.assertNotNull(previous);
        Assertions.assertNotNull(next);
        Assertions.assertEquals(Integer.parseInt(expectedPreviousResultIndex), previous.getLine().getIndex(), "Find previous from "+fromIndex+" wrong index");
        Assertions.assertEquals(LINES.get(Integer.parseInt(expectedPreviousResultIndex)), previous.getLine(), "Find previous from "+fromIndex+" wrong line");
        Assertions.assertEquals(Integer.parseInt(expectedNextResultIndex), next.getLine().getIndex(), "Find next from "+fromIndex+" wrong index");
        Assertions.assertEquals(LINES.get(Integer.parseInt(expectedNextResultIndex)), next.getLine(), "Find next from "+fromIndex+" wrong line");
    }

    @Test
    void findBookmarksEmpty() {
        Bookmarks bookmarks = new Bookmarks(false, testLogDispatcher);
        Assertions.assertEquals(Optional.empty(), bookmarks.findNext(0));
        Assertions.assertEquals(Optional.empty(), bookmarks.findPrevious(0));
    }

    @Test
    void toggleForcedBookmarks() {
        Bookmarks bookmarks = new Bookmarks(false, testLogDispatcher);
        bookmarks.toggle(LINES.get(3));
        bookmarks.addListener(listener);
        Assertions.assertFalse(bookmarks.isBookmarksDisplayForced());
        Assertions.assertFalse(bookmarks.isLineForcedVisible(LINES.get(3)));
        bookmarks.toggleForceBookmarksDisplay();
        Assertions.assertTrue(bookmarks.isBookmarksDisplayForced());
        Assertions.assertTrue(bookmarks.isLineForcedVisible(LINES.get(3)));
        Assertions.assertTrue(forcedDisplay);
    }

    @Test
    void clearBookmarks() {
        Bookmarks bookmarks = new Bookmarks(false, testLogDispatcher);
        bookmarks.addListener(listener);
        bookmarks.toggle(LINES.get(3));
        bookmarks.toggle(LINES.get(2));
        bookmarks.clear();
        Assertions.assertEquals(0, bookmarks.count());
        Assertions.assertTrue(removedBookmarks.containsAll(Arrays.asList(new Bookmark(LINES.get(2)), new Bookmark(LINES.get(3)))));
    }

    @Test
    void testExport() {
        Bookmarks bookmarks = new Bookmarks(false, testLogDispatcher);
        bookmarks.toggle(LINES.get(3));
        bookmarks.toggle(LINES.get(2));
        Collection<String> expected = Arrays.asList(
                LINES.get(2).getRaw(),
                LINES.get(3).getRaw()
        );
        Collection<String> exported = bookmarks.export(LineLabelDisplayMode.NONE);
        Assertions.assertEquals(expected, exported);

    }
}