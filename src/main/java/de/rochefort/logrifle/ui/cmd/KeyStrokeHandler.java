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

import com.googlecode.lanterna.input.KeyStroke;

import java.util.Map;

public class KeyStrokeHandler {
    private final Map<KeyStroke, String> keyMap;
    private final CommandHandler commandHandler;

    public KeyStrokeHandler(Map<KeyStroke, String> keyMap, CommandHandler commandHandler) {
        this.keyMap = keyMap;
        this.commandHandler = commandHandler;
    }

    public ExecutionResult handleKeyStroke(KeyStroke keyStroke) {
        String commandLine = keyMap.get(keyStroke);
        if (commandLine == null) {
            return new ExecutionResult(false);
        }
        return commandHandler.handle(commandLine);
    }
}