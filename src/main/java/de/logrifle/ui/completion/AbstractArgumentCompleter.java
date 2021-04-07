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

import java.util.Arrays;
import java.util.List;

public abstract class AbstractArgumentCompleter {
    private final List<String> commandNames;

    protected AbstractArgumentCompleter(String... commandNames) {
        this.commandNames = Arrays.asList(commandNames);
    }

    public List<String> getCommandNames() {
        return commandNames;
    }

    public abstract CompletionResult getCompletions(String currentInput);
}
