package de.logrifle.data.bookmarks;

import com.googlecode.lanterna.TextColor;
import de.logrifle.data.parsing.Line;
import de.logrifle.data.parsing.LineParser;
import de.logrifle.data.parsing.LineParserTextImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class BookmarksTest {
    private static final LineParser PARSER = new LineParserTextImpl();
    private static final List<Line> LINES = new ArrayList<>();

    @BeforeAll
    static void beforeAll() {
        LINES.add(PARSER.parse(0, "line content 0", "label", TextColor.ANSI.DEFAULT).getParsedLine());
        LINES.add(PARSER.parse(1, "line content 1", "label", TextColor.ANSI.DEFAULT).getParsedLine());
        LINES.add(PARSER.parse(2, "line content 2", "label", TextColor.ANSI.DEFAULT).getParsedLine());
        LINES.add(PARSER.parse(3, "line content 3", "label", TextColor.ANSI.DEFAULT).getParsedLine());
        LINES.add(PARSER.parse(4, "line content 4", "label", TextColor.ANSI.DEFAULT).getParsedLine());
        LINES.add(PARSER.parse(5, "line content 5", "label", TextColor.ANSI.DEFAULT).getParsedLine());
        LINES.add(PARSER.parse(6, "line content 6", "label", TextColor.ANSI.DEFAULT).getParsedLine());
        LINES.add(PARSER.parse(7, "line content 7", "label", TextColor.ANSI.DEFAULT).getParsedLine());
        LINES.add(PARSER.parse(8, "line content 8", "label", TextColor.ANSI.DEFAULT).getParsedLine());
    }

    @Test
    void toggleBookmark() {
        Bookmarks bookmarks = new Bookmarks();
        Line lineToBookmark = LINES.get(3);
        bookmarks.toggle(lineToBookmark);
        Assertions.assertEquals(1, bookmarks.count());
        Assertions.assertTrue(bookmarks.isLineBookmarked(lineToBookmark));
        Assertions.assertTrue(bookmarks.getAll().contains(new Bookmark(lineToBookmark)));
        bookmarks.toggle(lineToBookmark);
        Assertions.assertEquals(0, bookmarks.count());
        Assertions.assertFalse(bookmarks.isLineBookmarked(lineToBookmark));
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
        Bookmarks bookmarks = new Bookmarks();
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
        Bookmarks bookmarks = new Bookmarks();
        Assertions.assertEquals(Optional.empty(), bookmarks.findNext(0));
        Assertions.assertEquals(Optional.empty(), bookmarks.findPrevious(0));
    }

    @Test
    void removeBookmark() {
        Bookmarks bookmarks = new Bookmarks();
        Line lineToBookmark = LINES.get(3);
        bookmarks.toggle(lineToBookmark);
        bookmarks.remove(new Bookmark(lineToBookmark));
        Assertions.assertEquals(0, bookmarks.count());
        Assertions.assertFalse(bookmarks.isLineBookmarked(lineToBookmark));
    }
}