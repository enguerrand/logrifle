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

package de.rochefort.logrifle.ui.cmd;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class Command {
    private final String commandName;
    @Nullable
    private final String commandShortName;

    protected Command(String commandName, @Nullable String commandShortName) {
        this.commandName = commandName;
        this.commandShortName = commandShortName;
    }

    String getCommandName() {
        return commandName;
    }

    Optional<String> getCommandShortname() {
        return Optional.ofNullable(commandShortName);
    }

    protected abstract ExecutionResult execute(String args);
}
