/*
 *  Copyright 2020, Enguerrand de Rochefort
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

package de.logrifle.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CommandAutoCompleter {
    private final List<String> allCommands;
    private final int maximumCommandLength;

    public CommandAutoCompleter(List<String> allCommands) {
        this.allCommands = allCommands;
        this.maximumCommandLength = allCommands.stream()
                .map(String::length)
                .max(Comparator.naturalOrder())
                .orElse(0) + ":".length();
    }

    public int getMaximumCommandLength() {
        return maximumCommandLength;
    }

    public List<String> getMatching(String currentInput) {
        if (!currentInput.startsWith(":")) {
            return Collections.emptyList();
        }
        String currentCommand = currentInput.substring(1);
        return allCommands.stream()
                .filter(c -> c.startsWith(currentCommand))
                .collect(Collectors.toList());
    }

    public String complete(String currentInput) {
        // TODO
        return currentInput;
    }
}
