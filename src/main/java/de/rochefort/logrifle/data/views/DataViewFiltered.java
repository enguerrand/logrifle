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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DataViewFiltered implements DataView {
    private final DataView parentView;
    private final List<Line> visibleLines;
    private final boolean inverted;
    private final Pattern pattern;

    public DataViewFiltered(String regex, DataView parentView, boolean inverted) {
        this.parentView = parentView;
        this.inverted = inverted;
        this.pattern = Pattern.compile(regex);
        this.visibleLines = parentView.getAllLines().stream()
                .filter(this::lineMatches)
                .collect(Collectors.toList());
    }

    private boolean lineMatches(Line l) {
        boolean patternMatches = pattern.matcher(l.getRaw()).find();
        return inverted != patternMatches;
    }

    @Override
    public int getLineCount() {
        return this.visibleLines.size();
    }

    @Override
    public List<Line> getAllLines() {
        return new ArrayList<>(this.visibleLines);
    }

    @Nullable
    @Override
    public DataView getParentView() {
        return parentView;
    }
}
