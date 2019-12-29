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
    private boolean showLineLabels = false;
    private int horizontalScrollPosition = 0;
    private final Bookmarks bookmarks;
    private boolean followTail;

    LogView(LogDispatcher logDispatcher, HighlightsData highlightsData, LogLineRenderer logLineRenderer, Bookmarks bookmarks, boolean followTail) {
        this.logLineRenderer = logLineRenderer;
        this.logDispatcher = logDispatcher;
        this.highlightsData = highlightsData;
        this.bookmarks = bookmarks;
        LayoutManager layout = new GridLayout(1);
        panel = new Panel(layout);
        this.followTail = followTail;
        viewListener = new DataViewListener() {
            @Override
            public void onUpdated(DataView source) {
                UI.runLater(() -> {
                    if (!Objects.equals(source, lastView)) {
                        return;
                    }
                    if (LogView.this.followTail) {
                        moveFocusToEnd();
                    }
                    update(null, source);
                });
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

        int lineLabelLength = getLineLabelLength(dataView.getMaxLineLabelLength());
        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            boolean focused = i == this.logPosition.getFocusOffset();
            boolean hot = followTail && i == lines.size() - 1;
            AbstractComponent<?> label = logLineRenderer.render(line, dataView.getLineCount(), focused, lineLabelLength, horizontalScrollPosition, highlightsData.getHighlights(), this.bookmarks, hot);
            panel.addComponent(label);
        }
        this.lastView = dataView;
    }

    int getLineLabelLength(int maxLineLabelLength) {
        return showLineLabels ? maxLineLabelLength : 1;
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
        LogPosition old = this.logPosition;
        this.logPosition = this.logPosition.scroll(lineCountDelta);
        if (this.logPosition.isBefore(old)) {
            this.followTail = false;
        }
        return new ExecutionResult(true);
    }

    ExecutionResult scrollPage(float factor) {
        Panel panel = this.panel;
        if (panel == null) {
            return new ExecutionResult(false);
        }
        int visibleLineCount = getVisibleLinesCount();
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
        LogPosition oldLogPosition = this.logPosition;
        int focusedLineIndex = oldLogPosition.getFocusedLineIndex();
        ExecutionResult executionResult;
        executionResult = scrollVertically(index - focusedLineIndex);
        if (focusedLineIndex != index) {
            int newOffset = index - Math.min(lastView.getLineCount() - 1, Math.max(0, this.logPosition.getTopIndex()));
            this.logPosition = new LogPosition(this.logPosition.getTopIndex(), newOffset);
            executionResult = new ExecutionResult(true);
        }
        return executionResult;
    }

    ExecutionResult scrollToStart() {
        return scrollToLine(0);
    }

    ExecutionResult moveFocusToEnd() {
        DataView lastView = this.lastView;
        if (lastView == null) {
            return new ExecutionResult(false);
        }
        this.followTail = true;
        this.logPosition = new LogPosition(0, lastView.getLineCount() - 1);
        return new ExecutionResult(true);
    }

    ExecutionResult moveFocus(int lineCountDelta) {
        this.logPosition = this.logPosition.moveFocus(lineCountDelta);
        if (lineCountDelta <= 0) {
            this.followTail = false;
        }
        return new ExecutionResult(true);
    }

    private int getVisibleLinesCount() {
        return panel.getSize().getRows();
    }

    ExecutionResult toggleLineLabels() {
        this.showLineLabels = !this.showLineLabels;
        return new ExecutionResult(true);
    }

    ExecutionResult toggleFollowTail() {
        if (this.followTail) {
            this.followTail = false;
        } else {
            // This implicitly activates follow tail
            moveFocusToEnd();
        }
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
