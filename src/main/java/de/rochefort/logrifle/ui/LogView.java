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
import com.googlecode.lanterna.gui2.AbstractComponent;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.LayoutManager;
import com.googlecode.lanterna.gui2.Panel;
import de.rochefort.logrifle.data.parsing.Line;
import de.rochefort.logrifle.data.views.DataView;
import de.rochefort.logrifle.ui.cmd.ExecutionResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

class LogView {
    private final Panel panel;
    private final LogLineRenderer logLineRenderer = new DefaultLogLineRenderer();
    private LogPosition logPosition = new LogPosition(0,0);
    private @Nullable DataView lastView;

    LogView() {
        LayoutManager layout = new GridLayout(1);
        panel = new Panel(layout);
    }

    Panel getPanel() {
        return panel;
    }

    void update(@Nullable TerminalSize newTerminalSize, DataView dataView) {
        TerminalSize size = newTerminalSize != null ? newTerminalSize : panel.getSize();
        int rows = size.getRows();
        int maxLineCount = dataView.getLineCount();

        this.logPosition = this.logPosition.scrollIfRequiredByFocus(rows, maxLineCount);

        panel.removeAllComponents();
        this.logPosition = this.logPosition.ensureValid(maxLineCount);
        List<Line> lines = dataView.getLines(this.logPosition.getTopIndex(), Math.max(0, rows));

        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            boolean focused = i == this.logPosition.getFocusOffset();
            AbstractComponent<?> label = logLineRenderer.render(line, i+1, lines.size(), focused);
            panel.addComponent(label);
        }
    }

    ExecutionResult scroll(int lineCountDelta) {
        this.logPosition = this.logPosition.scroll(lineCountDelta);
        return new ExecutionResult(true);
    }

    ExecutionResult moveFocus(int lineCountDelta) {
        this.logPosition = this.logPosition.moveFocus(lineCountDelta);
        return new ExecutionResult(true);
    }

    int getFocusedLineIndex(){
        return this.logPosition.getFocusedLineIndex();
    }

    @Nullable
    private Line getFocusedLine(){
        DataView lastView = this.lastView;
        if (lastView == null) {
            return null;
        }
        return lastView.getLine(getFocusedLineIndex());
    }
}
