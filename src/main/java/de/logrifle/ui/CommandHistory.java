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

import java.util.LinkedList;
import java.util.function.Consumer;

class CommandHistory {
    private final LinkedList<String> commands;
    private int historyPosition;
    private String uncommittedInput;

    CommandHistory() {
        commands = new LinkedList<>();
        reset();
    }

    void append(String command) {
        commands.addFirst(command);
        reset();
    }

    void reset() {
        historyPosition = 0;
        uncommittedInput = "";
    }

    void back(String currentInput, Consumer<String> historicalCommandHandler) {
        if (historyPosition == 0) {
            uncommittedInput = currentInput;
        }
        historyPosition = Math.min(commands.size(), historyPosition + 1);
        if (historyPosition > 0) {
            historicalCommandHandler.accept(commands.get(historyPosition - 1));
        }
    }

    void forward(Consumer<String> historicalCommandHandler) {
        if (historyPosition > 0) {
            historyPosition--;
            if (historyPosition == 0) {
                historicalCommandHandler.accept(uncommittedInput);
            } else {
                historicalCommandHandler.accept(commands.get(historyPosition - 1));
            }
        }
    }
}
