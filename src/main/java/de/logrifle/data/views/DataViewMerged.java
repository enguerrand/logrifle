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
import de.logrifle.base.RateLimiter;
import de.logrifle.base.RateLimiterFactory;
import de.logrifle.data.parsing.Line;
import de.logrifle.ui.UI;
import de.logrifle.ui.cmd.ExecutionResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataViewMerged extends DataView {
    private final List<DataView> sourceViews;
    private final RateLimiter updater;
    private final List<Line> linesCache = new CopyOnWriteArrayList<>();
    private final Map<String, Integer> processedLinesMap = new HashMap<>();

    public DataViewMerged(Collection<? extends DataView> sourceViews, LogDispatcher logDispatcher, RateLimiterFactory factory) {
        super("Root View",
                TextColor.ANSI.DEFAULT,
                logDispatcher,
                0);
        this.sourceViews = new CopyOnWriteArrayList<>(sourceViews);
        updateMaxLineLabelLengths();
        this.updater = factory.newRateLimiter(this::handleUpdate, logDispatcher);
        logDispatcher.execute(() -> {
            for (DataView sourceView : sourceViews) {
                sourceView.addListener(this);
            }
        });
        this.updater.requestExecution();
    }

    @Override
    public List<Line> getAllLines() {
        return new ArrayList<>(linesCache);
    }

    @Override
    public void onFullUpdate(DataView source) {
        getLogDispatcher().checkOnDispatchThreadOrThrow();
        this.processedLinesMap.clear();
        this.linesCache.clear();
        this.updater.requestExecution();
    }

    @Override
    public void onIncrementalUpdate(DataView source, List<Line> newLines) {
        getLogDispatcher().checkOnDispatchThreadOrThrow();
        this.updater.requestExecution();
    }

    private void handleUpdate() {
        getLogDispatcher().checkOnDispatchThreadOrThrow();
        boolean fullUpdate = linesCache.isEmpty();
        List<Line> newLines = new ArrayList<>();
        for (DataView sourceView : sourceViews) {
            String viewId = sourceView.getId();
            int processedLinesCount = processedLinesMap.getOrDefault(viewId, 0);
            if (sourceView.getLineCount() > processedLinesCount) {
                List<Line> newLinesInView = sourceView.getLines(processedLinesCount, null);
                for (Line line : newLinesInView) {
                    if (line.isVisible()) {
                        newLines.add(line);
                    }
                }
                processedLinesMap.put(viewId, processedLinesCount + newLinesInView.size());
            }
        }

        // Now apply a new index for the merged view
        linesCache.addAll(newLines);
        linesCache.sort(Line.ORDERING_COMPARATOR);
        for (int i = 0; i < linesCache.size(); i++) {
            Line line = linesCache.get(i);
            line.setIndex(i);
        }

        if (fullUpdate) {
            fireUpdated();
        } else {
            fireUpdatedIncremental(newLines);
        }
    }

    @Override
    protected void clearCacheImpl(){
        getLogDispatcher().checkOnDispatchThreadOrThrow();
        invalidateLogPosition();
        processedLinesMap.clear();
        linesCache.clear();
        handleUpdate();
    }

    public List<DataView> getViews() {
        UI.checkGuiThreadOrThrow();
        return Collections.unmodifiableList(this.sourceViews);
    }

    ExecutionResult addView(DataView dataView) {
        UI.checkGuiThreadOrThrow();
        this.sourceViews.add(dataView);
        updateMaxLineLabelLengths();
        getLogDispatcher().execute(() -> {
            clearCache();
            dataView.addListener(this);
        });
        return new ExecutionResult(true);
    }

    /**
     * @throws IndexOutOfBoundsException when called with ane invalid index
     */
    DataView removeView(int viewIndex) {
        UI.checkGuiThreadOrThrow();
        DataView removed = this.sourceViews.remove(viewIndex);
        updateMaxLineLabelLengths();
        removed.destroy();
        return removed;
    }

    ExecutionResult toggleView(int viewIndex) {
        UI.checkGuiThreadOrThrow();
        try {
            this.sourceViews.get(viewIndex).toggleActive();
            updateMaxLineLabelLengths();
            getLogDispatcher().execute(this::clearCache);
            return new ExecutionResult(true);
        } catch (IndexOutOfBoundsException e) {
            return new ExecutionResult(false, "Invalid view index: "+viewIndex);
        }
    }

    private void updateMaxLineLabelLengths() {
        setMaxLineLabelLength(sourceViews.stream()
                .filter(DataView::isActive)
                .map(DataView::getMaxLineLabelLength)
                .max(Comparator.comparing(x -> x))
                .orElse(0));
    }
}
