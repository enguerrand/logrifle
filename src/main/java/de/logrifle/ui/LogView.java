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

package de.logrifle.ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.AbstractComponent;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Panel;
import de.logrifle.base.LogDispatcher;
import de.logrifle.data.bookmarks.Bookmarks;
import de.logrifle.data.highlights.HighlightsData;
import de.logrifle.data.parsing.Line;
import de.logrifle.data.views.DataView;
import de.logrifle.data.views.DataViewListener;
import de.logrifle.ui.cmd.ExecutionResult;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

class LogView {
    private final Panel panel;
    private final LogLineRenderer logLineRenderer;
    private final HighlightsData highlightsData;
    private LogPosition logPosition = new LogPosition(-1,0);
    private @Nullable DataView lastView;
    private final DataViewListener viewListener;
    private final LogDispatcher logDispatcher;
    private LineLabelDisplayMode lineLabelDisplayMode;
    private int horizontalScrollPosition = 0;
    private final Bookmarks bookmarks;
    private boolean followTail;
    private final LineDetailViewState lineDetailViewState = new LineDetailViewState();
    private final AtomicReference<Boolean> showLineNumbers = new AtomicReference<>(true);

    LogView(LogDispatcher logDispatcher, HighlightsData highlightsData, LogLineRenderer logLineRenderer, Bookmarks bookmarks, boolean followTail, LineLabelDisplayMode initialLineLabelDisplayMode) {
        lineLabelDisplayMode = initialLineLabelDisplayMode;
        this.logLineRenderer = logLineRenderer;
        this.logDispatcher = logDispatcher;
        this.highlightsData = highlightsData;
        this.bookmarks = bookmarks;
        GridLayout layout = new ZeroMarginsGridLayout(1);
        panel = new Panel(layout);
        this.followTail = followTail;
        viewListener = new DataViewListener() {
            @Override
            public void onFullUpdate(DataView source) {
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

            @Override
            public void onIncrementalUpdate(DataView source, List<Line> newLines) {
                onFullUpdate(source);
            }

            @Override
            public void onCacheCleared(DataView source) {
            }

            @Override
            public void onDestroyed(DataView source) {
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
        this.lineDetailViewState.updateScrollPosition(rows);
        int topLinesCountToSkip = this.lineDetailViewState.getTopLinesCountToSkip(lines);
        int maxRenderableLineCount = this.lineDetailViewState.computeRenderableLineCount(lines, rows);
        int maxLineIndex = Math.min(maxRenderableLineCount + topLinesCountToSkip, lines.size());
        for (int i = topLinesCountToSkip; i < maxLineIndex; i++) {
            Line line = lines.get(i);
            boolean focused = i == this.logPosition.getFocusOffset();
            boolean hot = followTail && i == lines.size() - 1;
            AbstractComponent<?> label = logLineRenderer.render(line, dataView.getLineCount(), focused, lineLabelLength, horizontalScrollPosition, highlightsData.getHighlights(), this.bookmarks, hot, this.lineDetailViewState, rows, showLineNumbers.get());
            panel.addComponent(label);
        }
        this.lastView = dataView;
    }

    int getLineLabelLength(int maxLineLabelLength) {
        return Math.min(this.lineLabelDisplayMode.getMaxLength(), maxLineLabelLength);
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

    ExecutionResult scrollToLineStart() {
        if (horizontalScrollPosition == 0) {
            return new ExecutionResult(false);
        }
        this.horizontalScrollPosition = 0;
        return new ExecutionResult(true);
    }

    ExecutionResult scrollToLineEnd() {
        Line focusedLine = getFocusedLine();
        DataView lastView = this.lastView;
        if (focusedLine == null || lastView == null) {
            return new ExecutionResult(false);
        }
        int marginWidth = ((GridLayout)panel.getLayoutManager()).getHorizontalSpacing();
        int columnsAvailable = panel.getSize().getColumns() - 2 * marginWidth;
        int lineLabelLength = getLineLabelLength(lastView.getMaxLineLabelLength());
        AbstractComponent<?> renderedLine = logLineRenderer.render(focusedLine, lastView.getLineCount(), false, lineLabelLength, 0, Collections.emptyList(), this.bookmarks, false, LineDetailViewState.IGNORED, 1, showLineNumbers.get());
        int lineLength = renderedLine.getPreferredSize().getColumns();
        this.horizontalScrollPosition = Math.max(0, lineLength - columnsAvailable);
        return new ExecutionResult(true);
    }

    ExecutionResult scrollVertically(int lineCountDelta) {
        int rows = panel.getSize().getRows();
        if (this.lineDetailViewState.isActive() && this.lineDetailViewState.needsScrolling(rows)) {
            return this.lineDetailViewState.scroll(lineCountDelta, rows);
        }
        this.lineDetailViewState.reset();
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

    ExecutionResult scrollToLine(Line line) {
        DataView lastView = this.lastView;
        if (lastView == null) {
            return new ExecutionResult(false);
        }
        int indexInLastView = lastView.indexOfClosestTo(line.getIndex(), getFocusedLineIndexInView());
        if (indexInLastView < 0) {
            return new ExecutionResult(false);
        } else {
            return scrollToLine(indexInLastView);
        }
    }

    ExecutionResult gotoLine(int lineIndex) {
        DataView lastView = this.lastView;
        if (lastView == null) {
            return new ExecutionResult(false);
        }
        int index = lastView.indexOfClosestTo(lineIndex, Math.max(getFocusedLineIndexInView(), 0));
        if (index < 0) {
            return new ExecutionResult(false);
        } else {
            return scrollToLine(index);
        }
    }

    ExecutionResult scrollToLine(int indexInCurrentView) {
        DataView lastView = this.lastView;
        if (lastView == null) {
            return new ExecutionResult(false);
        }
        LogPosition oldLogPosition = this.logPosition;
        int focusedLineIndex = oldLogPosition.getFocusedLineIndex();
        ExecutionResult executionResult;
        executionResult = scrollVertically(indexInCurrentView - focusedLineIndex);
        if (focusedLineIndex != indexInCurrentView) {
            int newOffset = indexInCurrentView - Math.min(lastView.getLineCount() - 1, Math.max(0, this.logPosition.getTopIndex()));
            this.logPosition = new LogPosition(this.logPosition.getTopIndex(), newOffset);
            executionResult = new ExecutionResult(true);
        }
        return executionResult;
    }

    ExecutionResult toggleDetailLine() {
        boolean nowActive = this.lineDetailViewState.toggle(getFocusedLine());
        if (nowActive) {
        	this.followTail = false;
        }
        return new ExecutionResult(true);
    }

    ExecutionResult scrollToStart() {
        return scrollToLine(0);
    }

    ExecutionResult moveFocusToEnd() {
        this.lineDetailViewState.reset();
        DataView lastView = this.lastView;
        if (lastView == null) {
            return new ExecutionResult(false);
        }
        this.followTail = true;
        this.logPosition = new LogPosition(0, lastView.getLineCount() - 1);
        return new ExecutionResult(true);
    }

    ExecutionResult moveFocus(int lineCountDelta) {
        int rows = panel.getSize().getRows();
        if (this.lineDetailViewState.isActive() && this.lineDetailViewState.needsScrolling(rows)) {
            return this.lineDetailViewState.scroll(lineCountDelta, rows);
        }
        this.lineDetailViewState.reset();
        this.logPosition = this.logPosition.moveFocus(lineCountDelta);
        if (lineCountDelta <= 0) {
            this.followTail = false;
        }
        return new ExecutionResult(true);
    }

    private int getVisibleLinesCount() {
        return panel.getSize().getRows();
    }

    ExecutionResult cycleLineLabelDisplayMode() {
        this.lineLabelDisplayMode = this.lineLabelDisplayMode.next();
        return new ExecutionResult(true);
    }

    ExecutionResult toggleFollowTail() {
        this.lineDetailViewState.reset();
        if (this.followTail) {
            this.followTail = false;
        } else {
            // This implicitly activates follow tail
            moveFocusToEnd();
        }
        return new ExecutionResult(true);
    }

    int getFocusedLineIndexInView(){
        return this.logPosition.getFocusedLineIndex();
    }

    int getHorizontalScrollPosition() {
        return horizontalScrollPosition;
    }

    int getGlobalIndexOfFocusedLineOrZero() {
        @Nullable Line focusedLine = getFocusedLine();
        if (focusedLine == null) {
            return 0;
        }
        return focusedLine.getIndex();
    }

    @Nullable
    Line getFocusedLine(){
        DataView lastView = this.lastView;
        if (lastView == null) {
            return null;
        }
        int focusedLineIndex = getFocusedLineIndexInView();
        if (focusedLineIndex < 0) {
            return null;
        }
        return lastView.getLine(focusedLineIndex);
    }

    public LineDetailViewState getLineDetailViewState() {
        return lineDetailViewState;
    }

    boolean isShowLineNumbers() {
        return showLineNumbers.get();
    }

    void toggleLineNumbers() {
        this.showLineNumbers.updateAndGet(value -> !value);
    }
}
