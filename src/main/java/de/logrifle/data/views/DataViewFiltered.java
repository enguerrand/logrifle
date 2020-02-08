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
import de.logrifle.ui.cmd.ExecutionResult;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DataViewFiltered extends DataView {
    private final List<Line> visibleLines = new CopyOnWriteArrayList<>();
    private final DataView parentView;
    private final boolean inverted;
    private String regex;
    private Pattern pattern;

    public DataViewFiltered(String regex, DataView parentView, boolean inverted, LogDispatcher logDispatcher) {
        super(deriveTitleFromRegex(regex, inverted), parentView.getViewColor(), logDispatcher, parentView.getMaxLineLabelLength());
        this.regex = regex;
        this.parentView = parentView;
        this.inverted = inverted;
        this.pattern = Pattern.compile(regex);
    }

    @NotNull
    private static String deriveTitleFromRegex(String regex, boolean inverted) {
        return (inverted ? "! " : "") + regex;
    }

    public String getRegex() {
        return regex;
    }

    public void updateTitle(String regex) {
        super.setTitle(deriveTitleFromRegex(regex, this.inverted));
    }

    private boolean lineMatches(Line l) {
        getLogDispatcher().checkOnDispatchThreadOrThrow();
        boolean patternMatches = l.contains(pattern);
        return inverted != patternMatches;
    }

    public ExecutionResult setPattern(String regex) {
        getLogDispatcher().checkOnDispatchThreadOrThrow();
        if (Objects.equals(this.pattern.pattern(), regex)) {
            return new ExecutionResult(false);
        }
        this.pattern = Pattern.compile(regex);
        onFullUpdate(parentView);
        return new ExecutionResult(true);
    }

    @Override
    public List<Line> getAllLines() {
        return new ArrayList<>(this.visibleLines);
    }

    @Override
    public void onFullUpdate(DataView source) {
        getLogDispatcher().checkOnDispatchThreadOrThrow();
        List<Line> sourceLines = source.getAllLines();
        this.visibleLines.clear();
        this.visibleLines.addAll(sourceLines.stream()
                .filter(this::lineMatches)
                .collect(Collectors.toList()));
        fireUpdated();
    }

    @Override
    public void onIncrementalUpdate(DataView source, List<Line> newLines) {
        getLogDispatcher().checkOnDispatchThreadOrThrow();
        List<Line> newMatchingLines = newLines.stream()
                .filter(this::lineMatches)
                .collect(Collectors.toList());
        if (newMatchingLines.isEmpty()) {
            return;
        }
        if (isFullUpdateRequired(this.visibleLines, newMatchingLines.get(0))) {
            onFullUpdate(source);
            return;
        }
        this.visibleLines.addAll(newMatchingLines);
        fireUpdatedIncremental(newMatchingLines);
    }

    private static boolean isFullUpdateRequired(List<Line> previouslyProcessed, Line firstNewMatchingLine) {
        if (previouslyProcessed.isEmpty()) {
            return false;
        }
        Line last = previouslyProcessed.get(previouslyProcessed.size() - 1);
        return last.getTimestamp() > firstNewMatchingLine.getTimestamp();
    }

    @Override
    protected void clearCacheImpl() {
        getLogDispatcher().checkOnDispatchThreadOrThrow();
        onFullUpdate(this);
    }
}
