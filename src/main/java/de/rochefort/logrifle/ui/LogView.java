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
import de.rochefort.logrifle.base.LogDispatcher;
import de.rochefort.logrifle.data.bookmarks.Bookmarks;
import de.rochefort.logrifle.data.highlights.HighlightsData;
import de.rochefort.logrifle.data.parsing.Line;
import de.rochefort.logrifle.data.views.DataView;
import de.rochefort.logrifle.data.views.DataViewListener;
import de.rochefort.logrifle.ui.cmd.ExecutionResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

class LogView {
    private final Panel panel;
    private final LogLineRenderer logLineRenderer;
    private final HighlightsData highlightsData;
    private LogPosition logPosition = new LogPosition(-1,0);
    private @Nullable DataView lastView;
    private final DataViewListener viewListener;
    private final LogDispatcher logDispatcher;
    private boolean showLineLabels = true;
    private int horizontalScrollPosition = 0;
    private final Bookmarks bookmarks;

    LogView(LogDispatcher logDispatcher, HighlightsData highlightsData, LogLineRenderer logLineRenderer, Bookmarks bookmarks) {
        this.logLineRenderer = logLineRenderer;
        this.logDispatcher = logDispatcher;
        this.highlightsData = highlightsData;
        this.bookmarks = bookmarks;
        LayoutManager layout = new GridLayout(1);
        panel = new Panel(layout);
        viewListener = new DataViewListener() {
            @Override
            public void onUpdated(DataView source) {
                UI.runLater(() ->
                        update(null, source));
            }
        };
    }

    Panel getPanel() {
        return panel;
    }

    void update(@Nullable TerminalSize newTerminalSize, DataView dataView) {
        updateListenerRegistrationIfNeeded(dataView);
        this.logPosition = this.logPosition.transferIfNeeded(this.lastView, dataView);
        TerminalSize size = newTerminalSize != null ? newTerminalSize : panel.getSize();
        int rows = size.getRows();
        int maxLineCount = dataView.getLineCount();

        this.logPosition = this.logPosition.scrollIfRequiredByFocus(rows, maxLineCount);

        panel.removeAllComponents();
        this.logPosition = this.logPosition.ensureValid(maxLineCount);
        List<Line> lines = dataView.getLines(this.logPosition.getTopIndex(), Math.max(0, rows));

        int maxLineLabelLength = dataView.getMaxLineLabelLength();

        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            boolean focused = i == this.logPosition.getFocusOffset();
            AbstractComponent<?> label = logLineRenderer.render(line, dataView.getLineCount(), focused, (showLineLabels ? maxLineLabelLength : 1), horizontalScrollPosition, highlightsData.getHighlights(), this.bookmarks);
            panel.addComponent(label);
        }
        this.lastView = dataView;
    }

    private void updateListenerRegistrationIfNeeded(DataView dataView) {
        if (!Objects.equals(this.lastView, dataView)) {
            logDispatcher.execute(() -> {
                if (this.lastView != null) {
                    this.lastView.removeListener(this.viewListener);
                }
                dataView.addListener(this.viewListener);
            });
        }
    }

    ExecutionResult scrollHorizontally(int columnCountDelta) {
        int currentHorizontalScrollPosition = this.horizontalScrollPosition;
        this.horizontalScrollPosition = Math.max(0, currentHorizontalScrollPosition + columnCountDelta);
        return new ExecutionResult(this.horizontalScrollPosition != currentHorizontalScrollPosition);
    }

    ExecutionResult scrollVertically(int lineCountDelta) {
        this.logPosition = this.logPosition.scroll(lineCountDelta);
        return new ExecutionResult(true);
    }

    ExecutionResult scrollPage(float factor) {
        Panel panel = this.panel;
        if (panel == null) {
            return new ExecutionResult(false);
        }
        int visibleLineCount = panel.getSize().getRows();
        int scrollLineCount;
        scrollLineCount = (int) (factor > 0
                ? Math.ceil(factor * visibleLineCount)
                : Math.floor(factor * visibleLineCount));
        return scrollVertically(scrollLineCount);
    }

    ExecutionResult scrollToLine(int index) {
        DataView lastView = this.lastView;
        if (lastView == null) {
            return new ExecutionResult(false);
        }
        int focusedLineIndex = getFocusedLineIndex();
        ExecutionResult executionResult = scrollVertically(index - focusedLineIndex);
        if (focusedLineIndex != index) {
            int newOffset = index - Math.min(lastView.getLineCount() - 1, Math.max(0, logPosition.getTopIndex()));
            this.logPosition = new LogPosition(this.logPosition.getTopIndex(), newOffset);
            return new ExecutionResult(true);
        } else {
            return executionResult;
        }
    }

    ExecutionResult scrollToStart() {
        return scrollToLine(0);
    }

    ExecutionResult scrollToEnd() {
        DataView lastView = this.lastView;
        if (lastView == null) {
            return new ExecutionResult(false);
        }
        return scrollToLine(lastView.getLineCount() -1);
    }

    ExecutionResult moveFocus(int lineCountDelta) {
        this.logPosition = this.logPosition.moveFocus(lineCountDelta);
        return new ExecutionResult(true);
    }

    ExecutionResult toggleLineLabels() {
        this.showLineLabels = !this.showLineLabels;
        return new ExecutionResult(true);
    }

    int getFocusedLineIndex(){
        return this.logPosition.getFocusedLineIndex();
    }

    int getHorizontalScrollPosition() {
        return horizontalScrollPosition;
    }

    @Nullable
    Line getFocusedLine(){
        DataView lastView = this.lastView;
        if (lastView == null) {
            return null;
        }
        int focusedLineIndex = getFocusedLineIndex();
        if (focusedLineIndex < 0) {
            return null;
        }
        return lastView.getLine(focusedLineIndex);
    }
}
