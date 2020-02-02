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

import de.logrifle.data.parsing.Line;
import de.logrifle.data.views.DataView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

class LogPosition {
    private final int topIndex;
    private final int focusOffset;

    LogPosition(int topIndex, int focusOffset) {
        this.topIndex = topIndex;
        this.focusOffset = focusOffset;
    }

    int getFocusOffset() {
        return focusOffset;
    }

    int getTopIndex() {
        return topIndex;
    }

    LogPosition scroll(int delta) {
        return new LogPosition(this.topIndex + delta, this.focusOffset);
    }

    LogPosition moveFocus(int delta) {
        return new LogPosition(this.topIndex, this.focusOffset + delta);
    }

    int getFocusedLineIndex(){
        return topIndex + focusOffset;
    }

    LogPosition ensureValid(int totalLineCount) {
        if (topIndex >= totalLineCount) {
            return new LogPosition(totalLineCount - 1, 0);
        } else if (topIndex + focusOffset >= totalLineCount) {
            return new LogPosition(topIndex, totalLineCount - topIndex - 1);
        } else if (topIndex < 0) {
            return new LogPosition(0, focusOffset)
                    .ensureValid(totalLineCount); // need to call again because previous conditions might be no longer met due to topIndex increase
        } else {
            return this;
        }
    }

    LogPosition scrollIfRequiredByFocus(int visibleRowsCount, int totalLineCount) {
        int nextTopIndex = this.topIndex;
        int nextFocusOffset = this.focusOffset;
        if (nextFocusOffset < 0) {
            nextTopIndex = Math.max(0, nextTopIndex + nextFocusOffset);
            nextFocusOffset = 0;
        }
        if (nextFocusOffset + nextTopIndex >= totalLineCount) {
            nextFocusOffset = totalLineCount - nextTopIndex - 1;
        }
        if (nextFocusOffset > visibleRowsCount - 1) {
            nextTopIndex = Math.min(totalLineCount, nextTopIndex + nextFocusOffset + 1 - visibleRowsCount);
            nextFocusOffset = visibleRowsCount - 1;
        }
        return new LogPosition(nextTopIndex, nextFocusOffset);
    }

    LogPosition transferIfNeeded(@Nullable DataView from, DataView to) {
        List<Line> allLines = to.getAllLines();
        if (allLines.isEmpty() || to.getAndClearLogPositionInvalidated()) {
            return new LogPosition(-1, 0);
        }
        if (Objects.equals(from, to)) {
            return this;
        }
        if (from == null) {
            return this;
        }

        Line focusedLine = getFocusedLine(from);
        int nextFocusIndex;
        if (focusedLine != null) {
            nextFocusIndex = getNextFocusIndex(allLines, focusedLine);
        } else {
            nextFocusIndex = -1;
        }
        int top = Math.max(0, nextFocusIndex - this.focusOffset);
        return new LogPosition(top, nextFocusIndex - top);
    }

    @Nullable
    private Line getFocusedLine(@NotNull DataView from) {
        Line focusedLine;
        int focusedLineIndex = getFocusedLineIndex();
        if (focusedLineIndex >= 0) {
            focusedLine = from.getLine(focusedLineIndex);
        } else {
            focusedLine = null;
        }
        return focusedLine;
    }

    static int getNextFocusIndex(List<Line> allLines, Line focusedLine) {
        int nextFocusIndex;
        nextFocusIndex = allLines.indexOf(focusedLine);
        if (nextFocusIndex < 0) {
            long focusedLineTimestamp = focusedLine.getTimestamp();
            for (int i = 0; i < allLines.size(); i++) {
                Line line = allLines.get(i);
                if (line.getTimestamp() >= focusedLineTimestamp) {
                    nextFocusIndex = i;
                    break;
                }
            }
            if (nextFocusIndex < 0) {
                nextFocusIndex = allLines.size() - 1;
            }
        }
        return nextFocusIndex;
    }

    boolean isBefore(LogPosition other) {
        return (this.getTopIndex() < other.getTopIndex() ||
                (this.getTopIndex() == other.getTopIndex() && this.getFocusOffset() < other.getFocusOffset()));
    }
}
