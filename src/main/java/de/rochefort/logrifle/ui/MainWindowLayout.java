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

package de.rochefort.logrifle.ui;

import com.googlecode.lanterna.TerminalSize;
import org.jetbrains.annotations.Nullable;

class MainWindowLayout {
    private final TerminalSize logViewSize;
    private final TerminalSize commandBarSize;

    private MainWindowLayout(TerminalSize logViewSize, TerminalSize commandBarSize) {
        this.logViewSize = logViewSize;
        this.commandBarSize = commandBarSize;
    }

    TerminalSize getLogViewSize() {
        return logViewSize;
    }

    TerminalSize getCommandBarSize() {
        return commandBarSize;
    }

    static MainWindowLayout compute(@Nullable TerminalSize terminalSize, int commandBarHeight, int sideBarWidth) {
        if(terminalSize == null) {
            return null;
        }
        TerminalSize cmd = new TerminalSize(terminalSize.getColumns() , commandBarHeight);
        TerminalSize log = new TerminalSize(terminalSize.getColumns() - sideBarWidth, terminalSize.getRows() - cmd.getRows());
        return new MainWindowLayout(
                log,
                cmd
        );
    }
}