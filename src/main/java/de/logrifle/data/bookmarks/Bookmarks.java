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

package de.logrifle.data.bookmarks;

import de.logrifle.base.Strings;
import de.logrifle.data.parsing.Line;
import de.logrifle.data.views.LineSource;
import de.logrifle.ui.cmd.ExecutionResult;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Bookmarks {
    private final SortedSet<Bookmark> bookmarks = new TreeSet<>(Comparator.comparing(b -> b.getLine().getIndex()));
    private final Charset charset;
    private final AtomicReference<Boolean> forceBookmarksVisible = new AtomicReference<>(false);

    public Bookmarks(Charset charset, boolean forcedBookmarksDisplay) {
        this.charset = charset;
        this.forceBookmarksVisible.set(forcedBookmarksDisplay);
    }

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

    public ExecutionResult toggleForceBookmarksDisplay() {
        this.forceBookmarksVisible.updateAndGet(b -> !b);
        return new ExecutionResult(true);
    }

    public boolean isLineForcedVisible(Line line) {
        return forceBookmarksVisible.get() && isLineBookmarked(line);
    }

    public Set<Bookmark> getAll() {
        return Collections.unmodifiableSortedSet(bookmarks);
    }

    public void removeBookmarksOf(LineSource lineSource) {
        this.bookmarks.removeIf(next -> next.getLine().belongsTo(lineSource));
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

    public void write(String path) throws IOException {
        Files.write(
                Paths.get(
                        Strings.expandPathPlaceHolders(path)
                ),
                bookmarks.stream()
                        .map(Bookmark::toWritableString)
                        .collect(Collectors.toList()),
                charset,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }
}