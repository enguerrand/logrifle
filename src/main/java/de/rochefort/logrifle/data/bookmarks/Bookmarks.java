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

package de.rochefort.logrifle.data.bookmarks;

import de.rochefort.logrifle.data.parsing.Line;
import de.rochefort.logrifle.ui.cmd.ExecutionResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Bookmarks {
    private final SortedSet<Bookmark> bookmarks = new TreeSet<>(Comparator.comparing(b -> b.getLine().getIndex()));

    public ExecutionResult toggle(Line line) {
        Bookmark bookmark = new Bookmark(line);
        if (this.bookmarks.contains(bookmark)) {
            this.bookmarks.remove(bookmark);
        } else {
            this.bookmarks.add(bookmark);
        }
        return new ExecutionResult(true);
    }

    public ExecutionResult remove(Bookmark bookmark) {
        boolean removed = this.bookmarks.remove(bookmark);
        return new ExecutionResult(removed);
    }

    public Set<Bookmark> getAll() {
        return Collections.unmodifiableSortedSet(bookmarks);
    }

    public boolean isLineBookmarked(Line line) {
        return bookmarks.stream()
                .anyMatch(b -> b.getLine().equals(line));
    }

    public int count() {
        return bookmarks.size();
    }

    public Optional<Bookmark> findNext(int fromLineIndex) {
        if (bookmarks.isEmpty()) {
            return Optional.empty();
        }
        for (Bookmark bookmark : bookmarks) {
            if (bookmark.getLine().getIndex() > fromLineIndex) {
                return Optional.of(bookmark);
            }
        }
        return Optional.of(bookmarks.first());
    }

    public Optional<Bookmark> findPrevious(int fromLineIndex) {
        if (bookmarks.isEmpty()) {
            return Optional.empty();
        }
        ArrayList<Bookmark> bookmarksList = new ArrayList<>(this.bookmarks);
        for (int i = bookmarksList.size()-1; i >= 0; i--) {
            Bookmark bookmark = bookmarksList.get(i);
            if (bookmark.getLine().getIndex() < fromLineIndex) {
                return Optional.of(bookmark);
            }

        }
        return Optional.of(this.bookmarks.last());
    }
}