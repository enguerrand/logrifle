/*
 *  Copyright 2021, Enguerrand de Rochefort
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

package de.logrifle.ui.completion;

import java.util.Collections;
import java.util.List;

public class CompletionResult {
    public static CompletionResult NO_MATCHES = new CompletionResult(Collections.emptyList(), Collections.emptyList());
    private final List<String> matches;
    private final List<String> matchingCompletions;

    CompletionResult(List<String> matches, List<String> matchingCompletions) {
        this.matches = matches;
        this.matchingCompletions = matchingCompletions;
    }

    public List<String> getMatches() {
        return matches;
    }

    public List<String> getMatchingCompletions() {
        return matchingCompletions;
    }
}