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

package de.logrifle.data.views;

import com.googlecode.lanterna.TextColor;
import de.logrifle.base.LogDispatcher;
import de.logrifle.data.parsing.Line;
import de.logrifle.ui.UI;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public abstract class DataView implements DataViewListener, LineSource {
    private final String id = UUID.randomUUID().toString();
    private String title;
    private final Set<DataViewListener> listeners = new LinkedHashSet<>();
    private final LogDispatcher logDispatcher;
    private volatile int maxLineLabelLength;
    private final TextColor viewColor;
    private final AtomicReference<Boolean> active = new AtomicReference<>(true);
    private final AtomicBoolean logPositionInvalidated = new AtomicBoolean(false);

    protected DataView(String title, TextColor viewColor, LogDispatcher logDispatcher, int maxLineLabelLength) {
        this.title = title;
        this.viewColor = viewColor;
        this.logDispatcher = logDispatcher;
        this.maxLineLabelLength = maxLineLabelLength;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        UI.checkGuiThreadOrThrow();
        this.title = title;
    }

    @Override
    public TextColor getViewColor() {
        return viewColor;
    }

    public int getMaxLineLabelLength() {
        return maxLineLabelLength;
    }

    public void setMaxLineLabelLength(int maxLineLabelLength) {
        this.maxLineLabelLength = maxLineLabelLength;
    }

    public Line getLine(int index) {
        return getAllLines().get(index);
    }
    public List<Line> getLines(int topIndex, @Nullable Integer maxCount) {
        List<Line> snapshot = getAllLines();
        if (snapshot == null || snapshot.isEmpty() || topIndex >= snapshot.size() || topIndex < 0) {
            return Collections.emptyList();
        }
        int topIndexCorrected = Math.max(0, topIndex);
        if (maxCount == null || snapshot.size() <= topIndexCorrected + maxCount) {
            return snapshot.subList(topIndexCorrected, snapshot.size());
        } else {
            return snapshot.subList(topIndexCorrected, topIndexCorrected + maxCount);
        }
    }
    public void addListener(DataViewListener listener) {
        logDispatcher.checkOnDispatchThreadOrThrow();
        this.listeners.add(listener);
    }
    public void removeListener(DataViewListener listener) {
        logDispatcher.checkOnDispatchThreadOrThrow();
        this.listeners.remove(listener);
    }

    public String getId() {
        return id;
    }

    protected void fireUpdatedIncremental(List<Line> newLines) {
        logDispatcher.checkOnDispatchThreadOrThrow();
        for (DataViewListener listener : this.listeners) {
            listener.onIncrementalUpdate(DataView.this, newLines);
        }
    }

    protected void fireUpdated() {
        logDispatcher.checkOnDispatchThreadOrThrow();
        for (DataViewListener listener : this.listeners) {
            listener.onFullUpdate(DataView.this);
        }
    }

    protected void fireCacheCleared() {
        logDispatcher.checkOnDispatchThreadOrThrow();
        for (DataViewListener listener : this.listeners) {
            listener.onCacheCleared(DataView.this);
        }
    }

    void toggleActive() {
        this.active.updateAndGet(value -> !value);
        getLogDispatcher().execute(this::fireUpdated);
    }

    public boolean isActive() {
        return active.get();
    }

    public int getLineCount() {
        return getAllLines().size();
    }

    public abstract List<Line> getAllLines();

    public int indexOfClosestTo(int indexToHit, int startSearchAt) {
        List<Line> allLines = getAllLines();
        Line from = allLines.get(startSearchAt);
        if (indexToHit == from.getIndex()) {
            return startSearchAt;
        }
        int delta = indexToHit > from.getIndex() ? 1 : -1;

        for (int i = startSearchAt; i >= 0 && i < allLines.size(); i += delta) {
            Line l = allLines.get(i);
            if ((delta > 0 && l.getIndex() >= indexToHit) || (delta < 0 && l.getIndex() <= indexToHit)) {
                return i;
            }
        }
        return -1;
    }

    protected LogDispatcher getLogDispatcher() {
        return logDispatcher;
    }

    public void destroy() {
    }

    public void invalidateLogPosition() {
        this.logPositionInvalidated.set(true);
    }

    public boolean getAndClearLogPositionInvalidated() {
        return logPositionInvalidated.getAndSet(false);
    }

    void clearCache() {
        clearCacheImpl();
        fireCacheCleared();
    }

    protected abstract void clearCacheImpl();

    @Override
    public void onCacheCleared(DataView source) {
        clearCache();
    }
}
