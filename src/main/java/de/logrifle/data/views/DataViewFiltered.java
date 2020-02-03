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


import de.logrifle.data.parsing.Line;
import de.logrifle.base.LogDispatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DataViewFiltered extends DataView {
    private List<Line> visibleLines = new ArrayList<>();
    private final DataView parentView;
    private final boolean inverted;
    private final Pattern pattern;

    public DataViewFiltered(String regex, DataView parentView, boolean inverted, LogDispatcher logDispatcher) {
        super((inverted ? "! " : "") + regex, parentView.getViewColor(), logDispatcher, parentView.getMaxLineLabelLength());
        this.parentView = parentView;
        this.inverted = inverted;
        this.pattern = Pattern.compile(regex);
    }

    private boolean lineMatches(Line l) {
        boolean patternMatches = l.contains(pattern);
        return inverted != patternMatches;
    }

    @Override
    public List<Line> getAllLines() {
        return new ArrayList<>(this.visibleLines);
    }

    @Override
    public void onUpdated(DataView source) {
        handleUpdate();
    }

    private void handleUpdate() {
        getLogDispatcher().checkOnDispatchThreadOrThrow();
        List<Line> sourceLines = parentView.getAllLines();
        this.visibleLines = sourceLines.stream()
                .filter(this::lineMatches)
                .collect(Collectors.toList());
        fireUpdated();
    }

    @Override
    protected void clearCacheImpl() {
        getLogDispatcher().checkOnDispatchThreadOrThrow();
        handleUpdate();
    }
}
