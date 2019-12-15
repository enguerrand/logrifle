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

import de.rochefort.logrifle.base.LogDispatcher;
import de.rochefort.logrifle.base.RateLimiter;
import de.rochefort.logrifle.data.parsing.Line;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

public class DataViewMerged extends DataView {
    private final Collection<? extends DataView> sourceViews;
    private final RateLimiter updater;
    private final List<Line> linesCache = new CopyOnWriteArrayList<>();
    private final Map<String, Integer> processedLinesMap = new HashMap<>();

    public DataViewMerged(Collection<? extends DataView> sourceViews, LogDispatcher logDispatcher, ScheduledExecutorService timerPool) {
        super(sourceViews.stream()
                .map(DataView::getTitle)
                .collect(Collectors.joining(" + ")), logDispatcher);
        this.sourceViews = sourceViews;
        this.updater = new RateLimiter(this::handleUpdate, logDispatcher, timerPool, 150);
        logDispatcher.execute(() -> {
            for (DataView sourceView : sourceViews) {
                sourceView.addListener(this);
            }
        });
        this.updater.requestExecution();
    }

    @Override
    public int getLineCount() {
        return sourceViews.stream()
                .mapToInt(DataView::getLineCount)
                .sum();
    }

    @Override
    public List<Line> getAllLines() {
        return linesCache;
    }

    @Override
    public void onUpdated(DataView source) {
        this.updater.requestExecution();
    }

    private void handleUpdate() {
        getLogDispatcher().checkOnDispatchThreadOrThrow();
        List<Line> newLines = new ArrayList<>();
        for (DataView sourceView : sourceViews) {
            String viewId = sourceView.getId();
            int processedLinesCount = processedLinesMap.getOrDefault(viewId, 0);
            if (sourceView.getLineCount() > processedLinesCount) {
                List<Line> newLinesInView = sourceView.getLines(processedLinesCount, null);
                newLines.addAll(newLinesInView);
                processedLinesMap.put(viewId, processedLinesCount + newLinesInView.size());
            }
        }
        newLines.sort(Comparator.comparing(Line::getTimestamp));
        linesCache.addAll(newLines);
        fireUpdated();
    }
}
