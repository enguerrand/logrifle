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

package de.rochefort.logrifle.data.views;

import de.rochefort.logrifle.data.parsing.Line;
import de.rochefort.logrifle.ui.UI;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class DataView implements DataViewListener {
    private final String title;
    private final Set<DataViewListener> listeners = new LinkedHashSet<>();
    protected DataView(String title) {
        this.title = title;
    }
    public String getTitle() {
        return this.title;
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
        UI.checkGuiThreadOrThrow();
        this.listeners.add(listener);
    }
    public void removeListener(DataViewListener listener) {
        UI.checkGuiThreadOrThrow();
        this.listeners.remove(listener);
    }

    /**
     * Must be executed on logdispatch thread
     */
    protected void fireUpdated() {
        for (DataViewListener listener : this.listeners) {
            listener.onUpdated(DataView.this);
        }
    }
    public abstract int getLineCount();
    public abstract List<Line> getAllLines();
}
