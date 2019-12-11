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

class LogView {
    private final Panel panel;
    private final LogLineRenderer logLineRenderer = new DefaultLogLineRenderer();
    private int topIndex = 0;
    private int focusOffset = 0;

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

        scrollIfRequiredByFocus(rows, maxLineCount);

        panel.removeAllComponents();
        if (topIndex >= maxLineCount) {
            topIndex = maxLineCount - 1;
        } else if (topIndex < 0) {
            topIndex = 0;
        }
        List<Line> lines = dataView.getLines(topIndex, Math.max(0, rows));

        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            boolean focused = i == focusOffset;
            AbstractComponent<?> label = logLineRenderer.render(line, i+1, lines.size(), focused);
            panel.addComponent(label);
        }
    }

    private void scrollIfRequiredByFocus(int visibleRowsCount, int maxLineCount) {
        if (focusOffset < 0) {
            topIndex = Math.max(0, topIndex + focusOffset);
            focusOffset = 0;
        }
        if (focusOffset > visibleRowsCount - 1) {
            topIndex = Math.min(maxLineCount, topIndex + focusOffset + 1 - visibleRowsCount);
            focusOffset = visibleRowsCount - 1;
        }
        if (focusOffset + topIndex >= maxLineCount) {
            focusOffset = maxLineCount - topIndex - 1;
        }

    }

    public ExecutionResult scroll(int lineCountDelta) {
        this.topIndex += lineCountDelta;
        return new ExecutionResult(true);
    }

    public ExecutionResult moveFocus(int lineCountDelta) {
        this.focusOffset += lineCountDelta;
        return new ExecutionResult(true);
    }

    public int getFocusedLineIndex(){
        return topIndex + focusOffset;
    }
}
