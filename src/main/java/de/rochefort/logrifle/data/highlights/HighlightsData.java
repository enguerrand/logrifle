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

package de.rochefort.logrifle.data.highlights;

import de.rochefort.logrifle.ui.cmd.ExecutionResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HighlightsData {
    private final List<Highlight> highlights;

    public HighlightsData() {
        highlights = new ArrayList<>();
    }

    public ExecutionResult addHighlight(Highlight highlight){
        this.highlights.add(highlight);
        return new ExecutionResult(true);
    }

    public ExecutionResult removeHighlight(int index) {
        if (index < 0 || index >= this.highlights.size()) {
            return new ExecutionResult(false, "No highlight found at index "+index);
        }
        this.highlights.remove(index);
        return new ExecutionResult(true);
    }

    public List<Highlight> getHighlights() {
        return Collections.unmodifiableList(highlights);
    }

}
