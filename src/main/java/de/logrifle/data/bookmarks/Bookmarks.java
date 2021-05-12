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

import de.logrifle.base.LogDispatcher;
import de.logrifle.data.parsing.Line;
import de.logrifle.data.parsing.Lines;
import de.logrifle.data.views.LineSource;
import de.logrifle.ui.LineLabelDisplayMode;
import de.logrifle.ui.cmd.ExecutionResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Bookmarks {
    private final Set<Bookmark> bookmarks = ConcurrentHashMap.newKeySet();
    private final Set<BookmarksListener> listeners = new LinkedHashSet<>();
    private final LogDispatcher dispatcher;
    private final AtomicReference<Boolean> forceBookmarksVisible = new AtomicReference<>(false);

    public Bookmarks(boolean forcedBookmarksDisplay, LogDispatcher dispatcher) {
        this.dispatcher = dispatcher;
        this.forceBookmarksVisible.set(forcedBookmarksDisplay);
    }

    public ExecutionResult toggle(Line line) {
        Bookmark bookmark = new Bookmark(line);
        if (this.bookmarks.contains(bookmark)) {
            this.bookmarks.remove(bookmark);
            fireRemoved(Collections.singleton(bookmark));
        } else {
            this.bookmarks.add(bookmark);
            fireAdded(Collections.singleton(bookmark));
        }
        return new ExecutionResult(true);
    }

    public ExecutionResult clear() {
        List<Bookmark> removed = new ArrayList<>(this.bookmarks);
        this.bookmarks.clear();
        fireRemoved(removed);
        return new ExecutionResult(!removed.isEmpty());
    }

    public ExecutionResult toggleForceBookmarksDisplay() {
        this.forceBookmarksVisible.updateAndGet(b -> !b);
        fireForcedDisplayChanged();
        return new ExecutionResult(false);
    }

    public boolean isLineForcedVisible(Line line) {
        return forceBookmarksVisible.get() && isLineBookmarked(line);
    }

    public SortedSet<Bookmark> getAll() {
        SortedSet<Bookmark> sorted = new TreeSet<>(Comparator.comparing(b -> b.getLine().getIndex()));
        sorted.addAll(bookmarks);
        return sorted;
    }

    public void removeBookmarksOf(LineSource lineSource) {
        List<Bookmark> toBeRemoved = this.bookmarks.stream()
                .filter(l -> l.getLine().belongsTo(lineSource))
                .collect(Collectors.toList());
        if (toBeRemoved.isEmpty()) {
            return;
        }
        toBeRemoved.forEach(this.bookmarks::remove);
        fireRemoved(toBeRemoved);
    }

    public boolean isLineBookmarked(Line line) {
        return bookmarks.stream()
                .anyMatch(b -> b.getLine().equals(line));
    }

    boolean isBookmarksDisplayForced() {
        return forceBookmarksVisible.get();
    }

    public int count() {
        return bookmarks.size();
    }

    public Optional<Bookmark> findNext(int fromLineIndex) {
        SortedSet<Bookmark> sorted = getAll();
        if (sorted.isEmpty()) {
            return Optional.empty();
        }
        for (Bookmark bookmark : sorted) {
            if (bookmark.getLine().getIndex() > fromLineIndex) {
                return Optional.of(bookmark);
            }
        }
        return Optional.of(sorted.first());
    }

    public Optional<Bookmark> findPrevious(int fromLineIndex) {
        SortedSet<Bookmark> sorted = getAll();
        if (sorted.isEmpty()) {
            return Optional.empty();
        }
        ArrayList<Bookmark> bookmarksList = new ArrayList<>(sorted);
        for (int i = bookmarksList.size() - 1; i >= 0; i--) {
            Bookmark bookmark = bookmarksList.get(i);
            if (bookmark.getLine().getIndex() < fromLineIndex) {
                return Optional.of(bookmark);
            }

        }
        return Optional.of(sorted.last());
    }

    public Collection<String> export(LineLabelDisplayMode lineLabelDisplayMode) {
        return Lines.export(
                getAll().stream()
                        .map(Bookmark::getLine)
                        .collect(Collectors.toList()),
                lineLabelDisplayMode
        );
    }

    public void addListener(BookmarksListener listener) {
        dispatcher.execute(() -> listeners.add(listener));
    }

    private void fireListeners(Consumer<BookmarksListener> action) {
        dispatcher.execute(() -> listeners.forEach(action));
    }

    private void fireAdded(Collection<Bookmark> added) {
        fireListeners(l -> l.added(this, added));
    }

    private void fireRemoved(Collection<Bookmark> removed) {
        fireListeners(l -> l.removed(this, removed));
    }

    private void fireForcedDisplayChanged() {
        fireListeners(l -> l.forcedDisplayChanged(this));
    }
}